package com.tripmate.ai;

import com.tripmate.entity.AiLog;
import com.tripmate.mapper.AiLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AI 调用日志服务
 * 落库 ai_log,记录输入/模型/耗时/状态/错误,对应文档"AI 评测与防护"中的日志要求
 * 落库失败不影响主流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiLogService {

  private final AiLogMapper aiLogMapper;

  /** 记录一次 AI 调用(成功) */
  public void logSuccess(Long userId, String prompt, String model, int tokens, long costMs) {
    try {
      AiLog l = new AiLog();
      l.setUserId(userId);
      l.setPrompt(prompt);
      l.setModel(model);
      l.setRequestTime(LocalDateTime.now().minusNanos(costMs * 1_000_000));
      l.setResponseTime(LocalDateTime.now());
      l.setStatus("SUCCESS");
      l.setTokens(tokens);
      aiLogMapper.insert(l);
    } catch (Exception e) {
      log.warn("写入 AI 日志失败: {}", e.getMessage());
    }
  }

  /** 记录一次 AI 调用(失败) */
  public void logFailure(Long userId, String prompt, String model, String errMsg) {
    try {
      AiLog l = new AiLog();
      l.setUserId(userId);
      l.setPrompt(prompt);
      l.setModel(model);
      l.setRequestTime(LocalDateTime.now());
      l.setResponseTime(LocalDateTime.now());
      l.setStatus("FAIL");
      l.setErrorMsg(errMsg);
      l.setTokens(0);
      aiLogMapper.insert(l);
    } catch (Exception e) {
      log.warn("写入 AI 失败日志失败: {}", e.getMessage());
    }
  }
}
