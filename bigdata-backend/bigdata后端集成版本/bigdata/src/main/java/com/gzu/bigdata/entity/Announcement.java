package com.gzu.bigdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 公告实体类
 */
@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;       // 标题
    private String content;     // 内容
    private Long creatorId;     // 发布者ID
    private Date createTime;    // 创建时间
    private Date updateTime;    // 更新时间
}