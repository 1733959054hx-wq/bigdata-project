package com.gzu.bigdata.controller;

import com.google.common.net.HttpHeaders;
import com.gzu.bigdata.common.Result;
import com.gzu.bigdata.entity.FileMetadata;
import com.gzu.bigdata.service.FileService;
import com.gzu.bigdata.utils.JwtUtil;
import com.collaborative.cloud.hdfs.HdfsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/file")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private HdfsService hdfsService;

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        return userId != null ? userId : 0L;
    }

    /**
     * ✨✨✨ 新增：移动文件 ✨✨✨
     */
    @PostMapping("/move")
    public Result<String> moveFile(@RequestBody Map<String, Object> params) {
        try {
            Long fileId = Long.valueOf(params.get("fileId").toString());
            Long targetParentId = params.get("targetParentId") != null ?
                    Long.valueOf(params.get("targetParentId").toString()) : 0L;

            boolean success = fileService.moveFile(fileId, targetParentId);
            if (success) {
                return Result.success("移动成功", null);
            } else {
                return Result.error("移动失败");
            }
        } catch (Exception e) {
            log.error("移动文件异常", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * ✨✨✨ 新增：获取所有文件夹列表 (供移动文件选择) ✨✨✨
     */
    @GetMapping("/folders")
    public Result<List<FileMetadata>> getAllFolders() {
        try {
            List<FileMetadata> folders = fileService.getAllFolders();
            return Result.success("获取文件夹列表成功", folders);
        } catch (Exception e) {
            log.error("获取文件夹列表失败", e);
            return Result.error("获取文件夹列表失败");
        }
    }

    @PostMapping("/upload")

    public Result<FileMetadata> upload(@RequestParam("file") MultipartFile file,
                                       // ✨ 新增参数，非必传，默认0
                                       @RequestParam(value = "parentId", required = false, defaultValue = "0") Long parentId,
                                       HttpServletRequest request) {
        if (file.isEmpty()) return Result.error("文件为空");
        Long userId = getUserId(request);
        if (userId == 0L) return Result.error(401, "请先登录");

        try {
            // ✨ 传入 parentId
            FileMetadata meta = fileService.uploadFile(file, userId, parentId);
            return Result.success("上传成功", meta);
        } catch (Exception e) {
            log.error("上传异常", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) {
        try {
            FileMetadata meta = fileService.getById(fileId);
            if (meta == null || meta.getIsDeleted()) {
                response.sendError(404, "文件不存在");
                return;
            }
            setupResponseHeaders(response, meta);
            fileService.downloadFile(fileId, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载异常", e);
        }
    }

    @PutMapping("/rename")
    public Result<String> renameFile(@RequestBody Map<String, Object> params) {
        Long fileId = Long.valueOf(params.get("id").toString());
        String newName = (String) params.get("newName");
        if (fileService.renameFile(fileId, newName)) return Result.success("重命名成功", null);
        return Result.error("重命名失败");
    }

    // --- HBase 相关 ---
    @GetMapping("/hbase/files")
    public ResponseEntity<List<String>> listHBaseFiles() {
        return ResponseEntity.ok(fileService.listHBaseFiles());
    }

    @GetMapping("/content/{fileId}")
    public ResponseEntity<?> getContent(@PathVariable String fileId) {
        String content = fileService.getFileContent(fileId);
        return content != null ? ResponseEntity.ok(content) : ResponseEntity.notFound().build();
    }

    @PostMapping("/content/{fileId}")
    public ResponseEntity<String> saveContent(@PathVariable String fileId, @RequestBody String content) {
        return fileService.saveFileContent(fileId, content) ? ResponseEntity.ok("保存成功") : ResponseEntity.status(500).body("保存失败");
    }

    @DeleteMapping("/content/{fileId}")
    public ResponseEntity<String> deleteContent(@PathVariable String fileId) {
        return fileService.deleteFileContent(fileId) ? ResponseEntity.ok("删除成功") : ResponseEntity.status(500).body("删除失败");
    }

    // 辅助方法
    private void setupResponseHeaders(HttpServletResponse response, FileMetadata meta) throws IOException {
        String fileName = URLEncoder.encode(meta.getFileName(), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + fileName);
        if (meta.getFileSize() != null) response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(meta.getFileSize()));
    }
}