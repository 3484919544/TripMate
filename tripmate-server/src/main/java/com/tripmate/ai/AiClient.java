package com.tripmate.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tripmate.common.BusinessException;
import com.tripmate.common.ResultCode;
import com.tripmate.config.AiConfig;
import com.tripmate.entity.Trip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * AI 客户端:调用大模型(兼容 OpenAI 协议的服务商均可)
 * 职责:组装请求 → HTTP 调用 → 超时/重试 → 解析为 Trip → 写日志
 * 切换模型只需改 AiConfig(base-url/api-key/model)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

  private final AiConfig aiConfig;
  private final AiResultParser parser;
  private final AiLogService aiLogService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  /**
   * 生成行程
   *
   * @param systemPrompt 系统提示
   * @param userPrompt   用户提示
   * @param logPrompt    写入 ai_log 的精简输入
   * @param userId       调用者
   * @param budget       预算(用于解析后校验)
   */
  public Trip generateTrip(String systemPrompt, String userPrompt, String logPrompt,
                           Long userId, BigDecimal budget) {
    int maxAttempt = aiConfig.getRetry() + 1;
    BusinessException last = null;
    for (int attempt = 1; attempt <= maxAttempt; attempt++) {
      long start = System.currentTimeMillis();
      try {
        String content = callModel(systemPrompt, userPrompt);
        long cost = System.currentTimeMillis() - start;
        Trip trip = parser.parse(content, budget);
        aiLogService.logSuccess(userId, logPrompt, aiConfig.getModel(), 0, cost);
        log.info("AI 生成成功: {} ms, {} 天", cost, trip.getDays());
        return trip;
      } catch (BusinessException e) {
        last = e;
        log.warn("AI 生成第 {} 次失败: {}", attempt, e.getMessage());
      } catch (Exception e) {
        last = new BusinessException(ResultCode.AI_CALL_FAILED, e.getMessage());
        log.warn("AI 生成第 {} 次异常: {}", attempt, e.getMessage());
      }
    }
    aiLogService.logFailure(userId, logPrompt, aiConfig.getModel(),
        last != null ? last.getMessage() : "unknown");
    throw last != null ? last : new BusinessException(ResultCode.AI_CALL_FAILED);
  }

  /** 调用大模型,返回 content 文本 */
  private String callModel(String systemPrompt, String userPrompt) {
    try {
      ObjectNode body = objectMapper.createObjectNode();
      body.put("model", aiConfig.getModel());
      body.put("temperature", aiConfig.getTemperature());
      if (aiConfig.isJsonMode()) {
        ObjectNode fmt = objectMapper.createObjectNode();
        fmt.put("type", "json_object");
        body.set("response_format", fmt);
      }
      ArrayNode messages = body.putArray("messages");
      ObjectNode sys = messages.addObject();
      sys.put("role", "system");
      sys.put("content", systemPrompt);
      ObjectNode usr = messages.addObject();
      usr.put("role", "user");
      usr.put("content", userPrompt);

      RequestBody reqBody = RequestBody.create(objectMapper.writeValueAsString(body), JSON);
      String url = aiConfig.getBaseUrl() + aiConfig.getEndpoint();
      Request req = new Request.Builder()
          .url(url)
          .header("Authorization", "Bearer " + aiConfig.getApiKey())
          .header("Content-Type", "application/json")
          .post(reqBody)
          .build();

      OkHttpClient client = new OkHttpClient.Builder()
          .connectTimeout(Duration.ofSeconds(10).toMillis(), TimeUnit.MILLISECONDS)
          .readTimeout(aiConfig.getTimeout(), TimeUnit.MILLISECONDS)
          .build();

      try (Response resp = client.newCall(req).execute()) {
        if (!resp.isSuccessful()) {
          String errBody = resp.body() != null ? resp.body().string() : "";
          log.warn("AI 接口返回 {} : {}", resp.code(), errBody);
          throw new BusinessException(ResultCode.AI_CALL_FAILED,
              "模型接口错误(" + resp.code() + ")");
        }
        String respStr = resp.body() != null ? resp.body().string() : "";
        JsonNode root = objectMapper.readTree(respStr);
        JsonNode choices = root.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
          throw new BusinessException(ResultCode.AI_FORMAT_ERROR);
        }
        JsonNode content = choices.get(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
          throw new BusinessException(ResultCode.AI_FORMAT_ERROR);
        }
        return content.asText();
      }
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.warn("AI 调用异常: {}", e.getMessage());
      throw new BusinessException(ResultCode.AI_CALL_FAILED, e.getMessage());
    }
  }
}
