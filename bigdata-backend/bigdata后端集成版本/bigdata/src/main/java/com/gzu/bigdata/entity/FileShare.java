package com.gzu.bigdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("file_share")
public class FileShare {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原文件ID
     */
    private Long fileId;

    /**
     * 原文件路径
     */
    private String originalPath;

    /**
     * 分享后路径
     */
    private String sharedPath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 分享者
     */
    private String sharedBy;

    /**
     * 分享时间
     */
    private Date sharedTime;

    /**
     * 过期天数
     */
    private Integer expireDays;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 是否删除
     */
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}