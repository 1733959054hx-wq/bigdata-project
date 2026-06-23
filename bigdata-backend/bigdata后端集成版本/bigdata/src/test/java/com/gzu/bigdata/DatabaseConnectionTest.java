package com.gzu.bigdata;  // 修改包名为你的实际包名

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("数据库连接成功！");
            System.out.println("数据库URL: " + connection.getMetaData().getURL());
            System.out.println("数据库用户名: " + connection.getMetaData().getUserName());
        } catch (Exception e) {
            System.out.println("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}