package com.gzu.bigdata.controller;

import com.gzu.bigdata.common.Result;
import com.gzu.bigdata.entity.FileMetadata;
import com.gzu.bigdata.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    @Autowired
    private FileService fileService;

    // 定义常量
    private static final String ROOT_PATH = "/BigData";

    /**
     * 创建目录
     * 修复：兼容前端传入 parentId 为路径字符串的情况
     */
    @PostMapping("/create")
    public Result<FileMetadata> createDirectoryJson(@RequestBody CreateDirectoryRequest request) {
        try {
            // 1. 解析 parentId
            Long parentId = 0L; // 默认为根目录
            Object inputId = request.getParentId();

            if (inputId instanceof Number) {
                // 如果前端传的是数字 ID
                parentId = ((Number) inputId).longValue();
            } else if (inputId instanceof String) {
                // 如果前端传的是路径字符串 (例如 "/BigData/test")
                String path = (String) inputId;
                if (!path.equals(ROOT_PATH) && !path.equals("/")) {
                    FileMetadata parentFolder = fileService.getFileByPath(path);
                    if (parentFolder != null) {
                        parentId = parentFolder.getId();
                    } else {
                        // 如果传入的父路径不存在，可以选择报错或默认在根目录
                        // 这里选择报错，避免创建在错误位置
                        return Result.error(400, "当前所在的父文件夹不存在或已被删除");
                    }
                }
            }

            // 2. 调用 Service 创建
            FileMetadata directory = fileService.createDirectory(request.getDirectoryName(), parentId);
            return Result.success("目录创建成功", directory);

        } catch (IllegalArgumentException e) {
            log.warn("创建目录参数错误: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建目录异常, request: {}", request, e);
            return Result.error("目录创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件列表（根据路径）
     */
    @GetMapping("/list")
    public Result<List<FileMetadata>> getFileList(
            @RequestParam(required = false) String path) {
        try {
            // 如果没传path参数，默认获取根目录
            if (path == null || path.trim().isEmpty()) {
                path = ROOT_PATH; // "/BigData"
            }

            List<FileMetadata> files = fileService.listFilesByPath(path);
            return Result.success("获取文件列表成功", files);

        } catch (Exception e) {
            log.error("获取文件列表异常, path: {}", path, e);
            return Result.error("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查目录是否存在
     */
    @GetMapping("/exists")
    public Result<Boolean> checkDirectoryExists(@RequestParam String directoryPath) {
        try {
            boolean exists = fileService.directoryExists(directoryPath);
            return Result.success("检查完成", exists);

        } catch (Exception e) {
            log.error("检查目录是否存在异常, path: {}", directoryPath, e);
            return Result.error("检查目录是否存在失败: " + e.getMessage());
        }
    }

    /**
     * 创建目录请求体
     */
    public static class CreateDirectoryRequest {
        private String directoryName;

        // ✨✨✨ 关键修改：将类型从 Long 改为 Object，以兼容 String 类型的路径 ✨✨✨
        private Object parentId;

        // getter and setter
        public String getDirectoryName() {
            return directoryName;
        }

        public void setDirectoryName(String directoryName) {
            this.directoryName = directoryName;
        }

        public Object getParentId() {
            return parentId;
        }

        public void setParentId(Object parentId) {
            this.parentId = parentId;
        }
    }
}