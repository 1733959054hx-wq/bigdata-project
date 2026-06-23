package com.gzu.bigdata.mapper;

import com.gzu.bigdata.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 不要添加任何自定义方法，保持最简单的继承
}