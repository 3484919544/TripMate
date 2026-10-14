package com.tripmate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 大模型配置(读取 ai.* 配置,兼容 OpenAI 协议,可切换不同服务商/模型)
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
  /** API Key */
  private String apiKey;
  /** 接口基址(填写你所用服务商的地址,不含 /chat/completions) */
  private String baseUrl;
  /** 模型名(填写你所用服务商的模型名称) */
  private String model;
  /** 接口路径,如 /chat/completions */
  private String endpoint;
  /** 超时毫秒 */
  private int timeout = 30000;
  /** 失败重试次数 */
  private int retry = 1;
  /** 温度 */
  private double temperature = 0.7;
  /** 是否启用 JSON 模式 */
  private boolean jsonMode = true;
}
