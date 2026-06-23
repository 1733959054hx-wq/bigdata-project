package com.gzu.bigdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzu.bigdata.entity.FileMetadata;
import com.gzu.bigdata.entity.FileShare;
import com.gzu.bigdata.mapper.FileShareMapper;
import com.gzu.bigdata.service.FileService;
import com.gzu.bigdata.service.FileShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FileShareServiceImpl extends ServiceImpl<FileShareMapper, FileShare> implements FileShareService {

    @Autowired
    private FileService fileMetadataService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileShare shareFile(String sourceFilePath, String sharedBy, Integer expireDays) {
        try {
            // 1. 查询源文件信息
            FileMetadata sourceFile = fileMetadataService.getFileByPath(sourceFilePath);
            if (sourceFile == null) {
                throw new RuntimeException("源文件不存在: " + sourceFilePath);
            }

            // 2. 构建分享路径
            String sharedFolderPath = "/我的分享";
            String fileName = extractFileName(sourceFilePath);
            String sharedPath = sharedFolderPath + "/" + fileName;

            // 3. 处理重名文件
            String finalSharedPath = generateUniquePath(sharedPath);

            // 4. 计算过期时间
            Date sharedTime = new Date();
            Date expireTime = calculateExpireTime(sharedTime, expireDays);

            // 5. 创建分享记录
            FileShare fileShare = new FileShare();
            fileShare.setFileId(sourceFile.getId());
            fileShare.setOriginalPath(sourceFilePath);
            fileShare.setSharedPath(finalSharedPath);
            fileShare.setFileName(fileName);
            fileShare.setFileSize(sourceFile.getFileSize());
            fileShare.setFileType(sourceFile.getFileType());
            fileShare.setSharedBy(sharedBy);
            fileShare.setSharedTime(sharedTime);
            fileShare.setExpireDays(expireDays);
            fileShare.setExpireTime(expireTime);
            fileShare.setIsDeleted(false);

            // 6. 复制物理文件
            copyPhysicalFile(sourceFilePath, finalSharedPath);

            // 7. 保存分享记录
            this.save(fileShare);

            log.info("文件分享成功: {} -> {}, 分享者: {}, 过期天数: {}",
                    sourceFilePath, finalSharedPath, sharedBy, expireDays);
            return fileShare;

        } catch (Exception e) {
            log.error("文件分享失败, sourcePath: {}, sharedBy: {}", sourceFilePath, sharedBy, e);
            throw new RuntimeException("文件分享失败: " + e.getMessage());
        }
    }

    @Override
    public List<FileShare> getSharesByUser(String sharedBy) {
        return this.list(new LambdaQueryWrapper<FileShare>()
                .eq(FileShare::getSharedBy, sharedBy)
                .eq(FileShare::getIsDeleted, false)
                .ge(FileShare::getExpireTime, new Date()) // 未过期的
                .orderByDesc(FileShare::getSharedTime));
    }

    @Override
    public List<FileShare> getAllValidShares() {
        return this.list(new LambdaQueryWrapper<FileShare>()
                .eq(FileShare::getIsDeleted, false)
                .ge(FileShare::getExpireTime, new Date())
                .orderByDesc(FileShare::getSharedTime));
    }

    @Override
    public boolean isShareExpired(Long shareId) {
        FileShare fileShare = this.getById(shareId);
        if (fileShare == null || fileShare.getIsDeleted()) {
            return true;
        }
        return fileShare.getExpireTime().before(new Date());
    }

    // 辅助方法
    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) return "unknown_file";
        int lastSlash = filePath.lastIndexOf("/");
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    private String generateUniquePath(String basePath) {
        // 检查路径是否已存在
        Long count = this.count(new LambdaQueryWrapper<FileShare>()
                .eq(FileShare::getSharedPath, basePath)
                .eq(FileShare::getIsDeleted, false));

        if (count == 0) {
            return basePath;
        }

        // 添加时间戳避免重名
        String nameWithoutExt = basePath.lastIndexOf(".") > 0 ?
                basePath.substring(0, basePath.lastIndexOf(".")) : basePath;
        String extension = basePath.lastIndexOf(".") > 0 ?
                basePath.substring(basePath.lastIndexOf(".")) : "";
        String timestamp = String.valueOf(System.currentTimeMillis());

        return nameWithoutExt + "_" + timestamp + extension;
    }

    private Date calculateExpireTime(Date sharedTime, Integer expireDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sharedTime);
        calendar.add(Calendar.DAY_OF_YEAR, expireDays);
        return calendar.getTime();
    }

    private void copyPhysicalFile(String sourcePath, String targetPath) {
        try {
            // 根据你的文件存储系统实现
            log.info("物理文件复制: {} -> {}", sourcePath, targetPath);
            // Files.copy(Paths.get(sourcePath), Paths.get(targetPath));
        } catch (Exception e) {
            log.error("复制文件失败: {} -> {}", sourcePath, targetPath, e);
            throw new RuntimeException("复制文件失败");
        }
    }




}