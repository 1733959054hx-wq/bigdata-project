package com.gzu.bigdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("file_metadata")
public class FileMetadata {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 原始文件名（用户上传时的文件名）
    private String fileName;

    // 存储的文件名（在 HDFS 中的实际文件名）
    private String storageName;

    // 文件在 HDFS 中的完整路径
    private String filePath;

    // 文件大小（字节）
    private Long fileSize;

    // 是否为文件夹
    private Boolean isFolder;

    // 文件类型（MIME类型）
    private String fileType;

    // 文件扩展名
    private String fileExtension;

    // 用户ID
    private Long userId;

    // 父目录ID
    private Long parentId;

    // 上传时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 删除时间
    private LocalDateTime deleteTime;

    // 逻辑删除标记
    @TableLogic
    @TableField(value = "is_deleted")
    private Boolean isDeleted;

    // 构造函数
    public FileMetadata() {
        this.isFolder = false;
        this.isDeleted = false;
    }

    // 便捷方法：获取文件扩展名
    public String getFileExtension() {
        if (fileExtension != null && !fileExtension.isEmpty()) {
            return fileExtension;
        }
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }

    // 便捷方法：获取纯文件名（不含扩展名）
    public String getFileNameWithoutExtension() {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }


}