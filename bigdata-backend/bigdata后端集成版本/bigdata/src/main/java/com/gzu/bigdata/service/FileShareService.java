package com.gzu.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gzu.bigdata.entity.FileShare;
import java.util.List;

public interface FileShareService extends IService<FileShare> {

    /**
     * 分享文件
     */
    FileShare shareFile(String sourceFilePath, String sharedBy, Integer expireDays);

    /**
     * 获取用户的分享列表
     */
    List<FileShare> getSharesByUser(String sharedBy);

    /**
     * 获取所有有效分享
     */
    List<FileShare> getAllValidShares();

    /**
     * 检查分享是否过期
     */
    boolean isShareExpired(Long shareId);
}