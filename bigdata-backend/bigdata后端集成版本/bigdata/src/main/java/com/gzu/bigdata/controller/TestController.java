package com.gzu.bigdata.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String hello() {
        return "Hello, Big Data Application! 应用启动成功！";
    }

    @GetMapping("/test")
    public String test() {
        return "测试接口正常工作！";
    }

    @GetMapping("/hdfs/status")
    public String hdfsStatus() {
        return "HDFS服务已连接：hdfs://node1:8020";
    }
}