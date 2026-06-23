package com.gzu.bigdata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;



@SpringBootApplication
@ComponentScan(basePackages = {"com.gzu.bigdata", "com.collaborative.cloud"})
public class BigdataApplication {
    public static void main(String[] args) {
        SpringApplication.run(BigdataApplication.class, args);
    }
}
