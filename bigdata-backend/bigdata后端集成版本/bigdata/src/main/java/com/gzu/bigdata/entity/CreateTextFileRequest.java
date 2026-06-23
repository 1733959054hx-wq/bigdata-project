package com.gzu.bigdata.entity;

import lombok.Data;

@Data
public class CreateTextFileRequest {
    private String fileName;  // 文件名（不需要加.txt后缀）
    private String content;   // 文件内容（可选）
    private Long parentId;    // 父目录ID（可选，不传就放在根目录）
}
