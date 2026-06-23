package com.gzu.bigdata.controller;

import com.gzu.bigdata.common.Result;
import com.gzu.bigdata.entity.FileShare;
import com.gzu.bigdata.entity.ShareFileRequest;
import com.gzu.bigdata.service.FileShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/share")
public class FileShareController {

    @Autowired
    private FileShareService fileShareService;

    /**
     * 分享文件
     */
    @PostMapping("/create")
    public Result<FileShare> shareFile(@RequestBody @Valid ShareFileRequest request) {
        try {
            FileShare fileShare = fileShareService.shareFile(
                    request.getSourceFilePath(),
                    request.getSharedBy(),
                    request.getExpireDays()
            );
            return Result.success("文件分享成功", fileShare);
        } catch (Exception e) {
            return Result.error(500, "文件分享失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的分享列表
     */
    @GetMapping("/my/{sharedBy}")
    public Result<List<FileShare>> getMyShares(@PathVariable String sharedBy) {
        try {
            List<FileShare> shares = fileShareService.getSharesByUser(sharedBy);
            return Result.success("获取分享列表成功", shares);
        } catch (Exception e) {
            return Result.error(500, "获取分享列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有分享
     */
    @GetMapping("/all")
    public Result<List<FileShare>> getAllShares() {
        try {
            List<FileShare> shares = fileShareService.getAllValidShares();
            return Result.success("获取所有分享成功", shares);
        } catch (Exception e) {
            return Result.error(500, "获取所有分享失败: " + e.getMessage());
        }
    }
}