package com.gzu.bigdata.controller;

import com.gzu.bigdata.common.Result;
import com.gzu.bigdata.entity.FileMetadata;
import com.gzu.bigdata.service.FileService;
import lombok.extern.slf4j.Slf4j; // ✨✨✨ 关键修正：从 slf44j 修正为 slf4j ✨✨✨
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trash")
public class FileTrashController {

    @Autowired
    private FileService fileService;

    /**
     * 获取回收站文件列表
     */
    @GetMapping("/list")
    public Result<List<FileMetadata>> getTrashList() {
        try {
            // TODO: 实际应用中，这里需要从Token获取当前用户ID进行过滤
            List<FileMetadata> files = fileService.listTrashFiles();
            return Result.success("获取回收站列表成功", files);

        } catch (Exception e) {
            log.error("获取回收站列表异常", e);
            return Result.error("获取回收站列表失败: " + e.getMessage());
        }
    }

    /**
     * 移动到回收站或永久删除（如果在回收站中）
     */
    @DeleteMapping("/{fileId}")
    public Result<String> moveToTrashOrDelete(@PathVariable Long fileId) {
        try {
            boolean success = fileService.moveToTrashOrDelete(fileId);

            if (success) {
                return Result.success("操作成功");
            } else {
                return Result.error("操作失败");
            }

        } catch (Exception e) {
            log.error("回收站操作异常, fileId: {}", fileId, e);
            return Result.error("操作异常: " + e.getMessage());
        }
    }

    /**
     * 从回收站恢复文件/目录
     */
    @PostMapping("/restore/{fileId}")
    public Result<String> restoreFromTrash(@PathVariable Long fileId,
                                           @RequestParam(required = false) Long targetParentId) {
        try {
            boolean success = fileService.restoreFromTrash(fileId, targetParentId);

            if (success) {
                return Result.success("文件恢复成功");
            } else {
                return Result.error("文件恢复失败");
            }

        } catch (Exception e) {
            log.error("从回收站恢复文件异常, fileId: {}", fileId, e);
            return Result.error("恢复异常: " + e.getMessage());
        }
    }

    /**
     * 清空回收站
     */
    @DeleteMapping("/empty")
    public Result<String> emptyTrash() {
        try {
            boolean success = fileService.emptyTrash();

            if (success) {
                return Result.success("回收站已清空");
            } else {
                return Result.error("清空回收站失败");
            }

        } catch (Exception e) {
            log.error("清空回收站异常", e);
            return Result.error("清空回收站异常: " + e.getMessage());
        }
    }

    /**
     * 批量移动到回收站或删除
     */
    @DeleteMapping("/batch")
    public Result<String> batchMoveToTrashOrDelete(@RequestBody List<Long> fileIds) {
        try {
            int successCount = 0;
            int totalCount = fileIds.size();

            for (Long fileId : fileIds) {
                boolean success = fileService.moveToTrashOrDelete(fileId);
                if (success) {
                    successCount++;
                }
            }

            if (successCount == totalCount) {
                return Result.success("所有文件操作成功");
            } else {
                return Result.success("部分文件操作成功，成功数量: " + successCount + "/" + totalCount);
            }

        } catch (Exception e) {
            log.error("批量回收站操作异常", e);
            return Result.error("批量操作异常: " + e.getMessage());
        }
    }
}