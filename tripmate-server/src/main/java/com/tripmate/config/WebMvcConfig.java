package com.tripmate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射:把本地 uploads/ 目录映射到 URL /api/uploads/**
 * (加 /api 前缀是为了匹配 vite 代理规则,前端请求 /api/uploads/xxx.jpg 时代理到后端)
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Value("${app.upload-dir:uploads}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 解析 uploadDir 为绝对路径
    String path = uploadDir;
    if (!path.startsWith("/") && !path.contains(":")) {
      path = System.getProperty("user.dir") + "/" + path;
    }
    registry.addResourceHandler("/api/uploads/**")
        .addResourceLocations("file:" + path + "/");
  }
}
