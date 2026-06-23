package com.gzu.bigdata.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ShareFileRequest {
    /**
     * 源文件路径
     */
    @NotBlank(message = "源文件路径不能为空")
    private String sourceFilePath;

    /**
     * 分享者
     */
    @NotBlank(message = "分享者不能为空")
    private String sharedBy;

    /**
     * 过期天数（可选：1, 7, 30, 90）
     */
    @NotNull(message = "过期天数不能为空")
    private Integer expireDays;
}