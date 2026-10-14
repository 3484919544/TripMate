package com.tripmate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智游 TripMate 后端启动类
 */
@SpringBootApplication
@MapperScan("com.tripmate.mapper")
public class TripMateApplication {
  public static void main(String[] args) {
    SpringApplication.run(TripMateApplication.class, args);
  }
}
