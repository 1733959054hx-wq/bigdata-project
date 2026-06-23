package com.gzu.bigdata;

import com.gzu.bigdata.entity.User;
import com.gzu.bigdata.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FastCloudStorageApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println("测试数据库连接...");
    }

    @Test
    void testUserMapper() {
        User user = new User();
        user.setUsername("t");
        user.setPassword("123456");
        user.setNickname("测试用户");

        int result = userMapper.insert(user);
        System.out.println("插入结果: " + result);
        System.out.println("用户ID: " + user.getId());
    }
}