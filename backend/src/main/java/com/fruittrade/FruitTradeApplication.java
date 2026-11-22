package com.fruittrade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 水果交易智能管理系统后端启动类。
 */
@SpringBootApplication
@MapperScan("com.fruittrade.mapper")
@EnableScheduling
public class FruitTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitTradeApplication.class, args);
    }
}
