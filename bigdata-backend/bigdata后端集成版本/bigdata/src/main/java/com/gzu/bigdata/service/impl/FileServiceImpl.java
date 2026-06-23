package com.gzu.bigdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.collaborative.cloud.hbase.HBaseException;
import com.collaborative.cloud.hbase.HBaseServiceImpl;
import com.collaborative.cloud.hdfs.HdfsException;
import com.collaborative.cloud.hdfs.HdfsService;
import com.gzu.bigdata.entity.FileMetadata;
import com.gzu.bigdata.mapper.FileMetadataMapper;
import com.gzu.bigdata.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileMetadataMapper, FileMetadata> implements FileService {

    @Autowired
    private HdfsService hdfsService;

    private HBaseServiceImpl hbaseService;

    private static final String TRASH_PATH = "/Trash";
    private static final String ROOT_PATH = "/BigData";

    public FileServiceImpl() {
        // 构造函数不初始化 HBase，使用 @PostConstruct
    }

    @PostConstruct
    public void init() {
        try {
            this.hbaseService = new HBaseServiceImpl();
            System.out.println("HBase 服务初始化成功");
        } catch (HBaseException e) {
            System.err.println("HBase 服务初始化失败: " + e.getMessage());
            // 不抛出异常，让应用继续运行
        }
    }

    // ==================== ✨ 核心移动功能 ====================

    @Override
    public List<FileMetadata> getAllFolders() {
        return list(new LambdaQueryWrapper<FileMetadata>()
                .eq(FileMetadata::getIsFolder, true)
                .eq(FileMetadata::getIsDeleted, false)
                .orderByAsc(FileMetadata::getFilePath));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveFile(Long fileId, Long targetParentId) {
        FileMetadata sourceFile = getById(fileId);
        if (sourceFile == null) {
            throw new RuntimeException("源文件不存在");
        }

        String targetFolderPath = ROOT_PATH;
        if (targetParentId != null && targetParentId != 0) {
            FileMetadata targetFolder = getById(targetParentId);
            if (targetFolder == null || !targetFolder.getIsFolder()) {
                throw new RuntimeException("目标文件夹不存在");
            }
            if (sourceFile.getIsFolder() && targetFolder.getFilePath().startsWith(sourceFile.getFilePath())) {
                throw new RuntimeException("不能移动到自己的子目录下");
            }
            targetFolderPath = targetFolder.getFilePath();
        }

        String oldPath = sourceFile.getFilePath();
        String fileName = sourceFile.getFileName();
        String newPath = targetFolderPath + "/" + fileName;

        if (oldPath.equals(newPath)) {
            return true;
        }

        FileMetadata existingFile = getFileByPath(newPath);
        if (existingFile != null) {
            throw new RuntimeException("目标文件夹下已存在同名文件: " + fileName);
        }

        try {
            log.info("HDFS移动: {} -> {}", oldPath, newPath);
            boolean hdfsSuccess = hdfsService.moveFile(oldPath, newPath);
            if (!hdfsSuccess) {
                throw new RuntimeException("HDFS 文件移动失败");
            }

            sourceFile.setParentId(targetParentId == null ? 0L : targetParentId);
            sourceFile.setFilePath(newPath);
            sourceFile.setUpdateTime(LocalDateTime.now());
            updateById(sourceFile);

            if (sourceFile.getIsFolder()) {
                log.info("递归更新子文件路径: {} -> {}", oldPath, newPath);
                baseMapper.updateChildPaths(oldPath, newPath);
            }

            return true;

        } catch (Exception e) {
            log.error("移动文件失败", e);
            throw new RuntimeException("移动文件失败: " + e.getMessage());
        }
    }

    // ==================== 其他基础功能 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean renameFile(Long fileId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("新名称不能为空");
        }
        String cleanName = newName.replaceAll("[/\\\\:*?\"<>|]", "_").trim();

        FileMetadata file = getById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        String oldName = file.getFileName();
        if (oldName.equals(cleanName)) {
            return true;
        }

        String oldPath = file.getFilePath();
        String parentPath = oldPath.contains("/") ? oldPath.substring(0, oldPath.lastIndexOf("/")) : "";
        String newPath = parentPath + "/" + cleanName;

        try {
            log.info("HDFS重命名: {} -> {}", oldPath, newPath);
            boolean hdfsSuccess = hdfsService.moveFile(oldPath, newPath);
            if (!hdfsSuccess) {
                throw new RuntimeException("HDFS 重命名操作失败");
            }

            file.setFileName(cleanName);
            file.setFilePath(newPath);
            if (file.getIsFolder()) {
                file.setStorageName(cleanName);
            }
            file.setUpdateTime(LocalDateTime.now());
            updateById(file);

            if (file.getIsFolder()) {
                log.info("批量更新子文件路径: {} -> {}", oldPath, newPath);
                baseMapper.updateChildPaths(oldPath, newPath);
            }

            return true;

        } catch (Exception e) {
            log.error("重命名失败: fileId={}, newName={}", fileId, cleanName, e);
            throw new RuntimeException("重命名失败: " + e.getMessage());
        }
    }

    /**
     * ✨ 已修复：支持上传到指定文件夹 (parentId)
     */
    @Override
    public FileMetadata uploadFile(MultipartFile file, Long userId, Long parentId) throws IOException {
        // 1. 计算父目录路径
        String parentPath = ROOT_PATH;
        long finalParentId = 0L;

        if (parentId != null && parentId != 0) {
            FileMetadata parent = getById(parentId);
            if (parent != null && parent.getIsFolder()) {
                parentPath = parent.getFilePath();
                finalParentId = parentId;
            }
        }

        // 2. 确保目录存在
        try {
            if (finalParentId == 0L && !hdfsService.mkdir(ROOT_PATH)) {
                log.warn("HDFS根目录已存在或创建失败 (非致命)");
            }
        } catch (HdfsException e) {
            log.error("检查或创建HDFS目录失败: {}", e.getMessage());
        }

        // 3. 生成 HDFS 路径
        String originalFilename = file.getOriginalFilename();
        String hdfsFilename = generateHdfsFilename(originalFilename); // 这里调用了下面的私有方法
        String hdfsPath = parentPath + "/" + hdfsFilename;

        // 4. 上传到 HDFS
        boolean uploadSuccess = hdfsService.uploadFile(file.getInputStream(), hdfsPath);
        if (!uploadSuccess) {
            throw new IOException("文件上传到 HDFS 失败: " + hdfsPath);
        }

        // 5. 创建文件元数据
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(originalFilename);
        metadata.setStorageName(hdfsFilename);
        metadata.setFilePath(hdfsPath);
        metadata.setFileSize(file.getSize());
        metadata.setIsFolder(false);
        metadata.setFileType(file.getContentType());
        metadata.setFileExtension(getFileExtension(originalFilename)); // 这里调用了下面的私有方法
        metadata.setUserId(userId);
        metadata.setParentId(finalParentId);
        metadata.setIsDeleted(false);

        // 6. 保存元数据
        save(metadata);

        log.info("文件成功上传到 HDFS: {}", hdfsPath);
        return metadata;
    }

    // 为了兼容旧接口 (如果还有其他地方调用双参数版本)，保留一个重载方法

    public FileMetadata uploadFile(MultipartFile file, Long userId) throws IOException {
        return uploadFile(file, userId, 0L);
    }

    @Override
    public String getFilePathById(Long fileId) {
        FileMetadata metadata = getById(fileId);
        if (metadata == null || metadata.getIsDeleted()) {
            return null;
        }
        return metadata.getFilePath();
    }

    @Override
    public boolean downloadFile(Long fileId, OutputStream outputStream) {
        try {
            FileMetadata metadata = getById(fileId);
            if (metadata == null || metadata.getIsDeleted()) {
                return false;
            }
            return hdfsService.downloadFile(metadata.getFilePath(), outputStream);
        } catch (Exception e) {
            log.error("文件下载异常, fileId: {}", fileId, e);
            return false;
        }
    }

    @Override
    public boolean moveToTrashOrDelete(Long fileId) {
        try {
            FileMetadata metadata = getById(fileId);

            if (metadata == null) {
                metadata = baseMapper.selectByIdPhysical(fileId);
            }

            if (metadata == null) return false;

            // 判断逻辑保持不变
            if (metadata.getIsDeleted()) {
                // 已经在回收站 -> 执行永久删除
                return deletePermanentlyFromTrash(fileId);
            } else {
                // 正常文件 -> 移入回收站
                return moveToTrash(fileId);
            }
        } catch (Exception e) {
            log.error("回收站操作异常", e);
            return false;
        }
    }

    @Override

    public boolean restoreFromTrash(Long fileId, Long targetParentId) {
        try {
            // 1. 使用 selectByIdPhysical 查询（之前教你加的，如果还没加，记得加上）
            FileMetadata metadata = baseMapper.selectByIdPhysical(fileId);

            if (metadata == null || !metadata.getIsDeleted()) {
                return false;
            }

            String fileName = extractFileName(metadata.getFilePath());
            String targetPath = ROOT_PATH + "/" + fileName;

            if (targetParentId != null) {
                FileMetadata parent = getById(targetParentId);
                if (parent != null && parent.getIsFolder()) {
                    targetPath = parent.getFilePath() + "/" + fileName;
                }
            }

            // 2. HDFS 移动操作
            boolean moveSuccess = hdfsService.moveFile(metadata.getFilePath(), targetPath);
            if (!moveSuccess) return false;

            // 3. ✨✨✨ 核心修改：使用自定义 Mapper 方法强制更新数据库 ✨✨✨
            // 确定最终的 parentId (如果没传 targetParentId，默认归位到根目录 0)
            Long finalParentId = (targetParentId != null) ? targetParentId : 0L;

            // 调用我们在 Mapper 里新写的 SQL
            return baseMapper.restoreFile(fileId, targetPath, finalParentId) > 0;

        } catch (Exception e) {
            log.error("恢复文件异常", e);
            return false;
        }
    }
    @Override
    public boolean emptyTrash() {
        try {
            List<FileMetadata> trashFiles = baseMapper.selectFilesInTrashPath();
            if (trashFiles.isEmpty()) return true;

            for (FileMetadata file : trashFiles) {
                hdfsService.deleteFile(file.getFilePath());
                baseMapper.physicalDeleteById(file.getId());
            }
            return true;
        } catch (Exception e) {
            log.error("清空回收站异常", e);
            return false;
        }
    }

    @Override
    public FileMetadata createDirectory(String directoryName, Long parentId) {
        try {
            if (directoryName == null || directoryName.trim().isEmpty()) {
                throw new IllegalArgumentException("目录名不能为空");
            }
            String cleanName = directoryName.replaceAll("[/\\\\:*?\"<>|]", "_").trim();

            String parentPath = ROOT_PATH;
            if (parentId != null) {
                FileMetadata parent = getById(parentId);
                if (parent != null && parent.getIsFolder()) {
                    parentPath = parent.getFilePath();
                }
            }

            String fullPath = parentPath + "/" + cleanName;
            if (directoryExists(fullPath)) {
                throw new IllegalArgumentException("目录已存在");
            }

            if (!hdfsService.mkdir(fullPath)) {
                throw new RuntimeException("HDFS创建目录失败");
            }

            FileMetadata dir = new FileMetadata();
            dir.setFileName(cleanName);
            dir.setStorageName(cleanName);
            dir.setFilePath(fullPath);
            dir.setFileSize(0L);
            dir.setIsFolder(true);
            dir.setFileType("directory");
            dir.setUserId(0L);
            dir.setParentId(parentId);
            dir.setIsDeleted(false);

            save(dir);
            return dir;

        } catch (Exception e) {
            log.error("创建目录失败", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean directoryExists(String path) {
        return count(new LambdaQueryWrapper<FileMetadata>()
                .eq(FileMetadata::getFilePath, path)
                .eq(FileMetadata::getIsFolder, true)
                .eq(FileMetadata::getIsDeleted, false)) > 0;
    }

    @Override
    public List<FileMetadata> listFilesByPath(String path) {
        String normalizedPath = (path == null || path.trim().isEmpty()) ? ROOT_PATH : path;
        if (!normalizedPath.startsWith("/")) normalizedPath = "/" + normalizedPath;

        return baseMapper.selectList(new LambdaQueryWrapper<FileMetadata>()
                .likeRight(FileMetadata::getFilePath, normalizedPath)
                .ne(FileMetadata::getFilePath, normalizedPath)
                .likeRight(FileMetadata::getFilePath, normalizedPath + "/")
                .notLike(FileMetadata::getFilePath, normalizedPath + "/%/%")
                .eq(FileMetadata::getIsDeleted, false));
    }

    @Override
    public FileMetadata getFileByPath(String filePath) {
        List<FileMetadata> list = baseMapper.selectList(new LambdaQueryWrapper<FileMetadata>()
                .eq(FileMetadata::getFilePath, filePath).eq(FileMetadata::getIsDeleted, false));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public boolean deletePermanently(Long fileId) {
        FileMetadata file = getById(fileId);
        if (file == null) return false;
        try {
            hdfsService.deleteFile(file.getFilePath());
            deleteFileContent(fileId.toString());
        } catch (Exception e) {
            log.warn("物理删除文件资源失败: {}", e.getMessage());
        }
        return removeById(fileId);
    }

    @Override
    public List<FileMetadata> listFiles(Long userId, Long parentId) {
        QueryWrapper<FileMetadata> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("is_deleted", false);
        if (parentId != null) query.eq("parent_id", parentId);
        else query.isNull("parent_id");
        return baseMapper.selectList(query);
    }

    /**
     * ✨ 已修复：使用原生 SQL 查询回收站，绕过逻辑删除过滤
     */
    @Override
    public List<FileMetadata> listTrashFiles() {
        return baseMapper.selectFilesInTrashPath();
    }

    // ==================== HBase 相关 ====================

    @Override
    public boolean saveFileContent(String fileId, String content) {
        if (hbaseService == null) return false;
        try { return hbaseService.saveContent(fileId, content); }
        catch (Exception e) { log.error("HBase保存失败", e); return false; }
    }

    @Override
    public String getFileContent(String fileId) {
        if (hbaseService == null) return null;
        try { return hbaseService.getContent(fileId); }
        catch (Exception e) { log.error("HBase读取失败", e); return null; }
    }

    @Override
    public List<String> listFileContentsByUser(String userId) {
        return new ArrayList<>();
    }

    @Override
    public boolean deleteFileContent(String fileId) {
        if (hbaseService == null) return false;
        try { return hbaseService.deleteContent(fileId); }
        catch (Exception e) { return false; }
    }

    @Override
    public List<String> listHBaseFiles() {
        if (hbaseService == null) return new ArrayList<>();
        try { return hbaseService.listFiles(); }
        catch (Exception e) { return new ArrayList<>(); }
    }

    // ==================== 🔒 私有辅助方法 (修复报错的关键) ====================

    private boolean moveToTrash(Long fileId) {
        FileMetadata m = getById(fileId);
        if (m == null) return false;
        String trashPath = TRASH_PATH + "/" + extractFileName(m.getFilePath());
        try {
            hdfsService.moveFile(m.getFilePath(), trashPath);
            return update(new LambdaUpdateWrapper<FileMetadata>()
                    .eq(FileMetadata::getId, fileId)
                    .set(FileMetadata::getIsDeleted, 1) // 设置为已删除
                    .set(FileMetadata::getDeleteTime, LocalDateTime.now())
                    .set(FileMetadata::getFilePath, trashPath));
        } catch (Exception e) {
            log.error("移入回收站失败", e);
            return false;
        }
    }

    private boolean deletePermanentlyFromTrash(Long fileId) {
        FileMetadata m = baseMapper.selectByIdPhysical(fileId);
        if (m == null) return false;
        try {
            hdfsService.deleteFile(m.getFilePath());
            return baseMapper.physicalDeleteById(fileId) > 0;
        } catch (Exception e) {
            log.error("永久删除失败", e);
            return false;
        }
    }

    private boolean isInTrash(String path) {
        return path != null && path.startsWith(TRASH_PATH);
    }

    private String extractFileName(String path) {
        if (path == null || path.isEmpty()) return "unknown";
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    // ✨ 修复报错：这两个方法必须在类里面
    private String generateHdfsFilename(String original) {
        String ext = getFileExtension(original);
        String name = original;
        if (name != null && name.contains(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }
        return name + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6) + ext;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    @PreDestroy
    public void destroy() {
        try {
            if (hbaseService != null) hbaseService.close();
            if (hdfsService != null) hdfsService.close();
        } catch (Exception e) {
            log.error("资源关闭失败", e);
        }
    }
}