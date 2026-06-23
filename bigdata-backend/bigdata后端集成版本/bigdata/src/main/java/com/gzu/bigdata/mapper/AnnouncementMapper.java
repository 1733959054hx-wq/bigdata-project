package com.gzu.bigdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzu.bigdata.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
    // 这里不需要写代码，BaseMapper 已经包含了增删改查
}