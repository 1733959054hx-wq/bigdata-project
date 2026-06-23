package com.gzu.bigdata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储相关配置
 * 方便后期如果迁移到 HDFS 或 OSS，只需要改路径配置即可
 */
@Configuration
public class FileStorageConfig {

    // 文件上传保存的基础路径，可以在 application.yml 中修改
    @Value("${file.storage-path:D:/uploads/}")
    private String storagePath;

    public String getStoragePath() {
        return storagePath;
    }
}
