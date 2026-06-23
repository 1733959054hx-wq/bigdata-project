package com.gzu.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gzu.bigdata.entity.FileMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 文件业务接口
 */
public interface FileService extends IService<FileMetadata> {

    FileMetadata uploadFile(MultipartFile file, Long userId, Long parentId) throws IOException;
    String getFilePathById(Long fileId);
    boolean downloadFile(Long fileId, OutputStream outputStream);
    boolean moveToTrashOrDelete(Long fileId);
    boolean restoreFromTrash(Long fileId, Long targetParentId);
    boolean emptyTrash();
    FileMetadata createDirectory(String directoryName, Long parentId);
    boolean directoryExists(String directoryPath);
    List<FileMetadata> listFilesByPath(String path);
    FileMetadata getFileByPath(String filePath);

    // HBase 相关
    boolean saveFileContent(String fileId, String content);
    String getFileContent(String fileId);
    List<String> listFileContentsByUser(String userId);
    boolean deleteFileContent(String fileId);
    List<String> listHBaseFiles();

    boolean deletePermanently(Long fileId);
    List<FileMetadata> listFiles(Long userId, Long parentId);
    List<FileMetadata> listTrashFiles();

    // 重命名
    boolean renameFile(Long fileId, String newName);

    // ✨✨✨ 新增：移动文件 ✨✨✨
    boolean moveFile(Long fileId, Long targetParentId);

    // ✨✨✨ 新增：获取所有文件夹（用于移动时的选项列表） ✨✨✨
    List<FileMetadata> getAllFolders();
}