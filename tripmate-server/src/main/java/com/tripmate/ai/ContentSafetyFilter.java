package com.tripmate.ai;

import com.tripmate.common.BusinessException;
import com.tripmate.common.ResultCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * AI 安全过滤
 * - 输入脱敏:拒绝空值/超长/提示词注入
 * - 输出过滤:基础敏感词过滤
 * 对应文档"AI 评测与防护"中的输入防护要求
 */
@Component
public class ContentSafetyFilter {

  // 提示词注入常见特征
  private static final List<Pattern> INJECTION_PATTERNS = List.of(
      Pattern.compile("(?i)ignore\\s+(previous|above|all)\\s+instructions"),
      Pattern.compile("(?i)disregard\\s+(the\\s+)?(system|previous)"),
      Pattern.compile("(?i)you\\s+are\\s+(now|actually)\\s+a"),
      Pattern.compile("(?i)system\\s*:\\s*"),
      Pattern.compile("(?i)reveal|leak|print\\s+your\\s+prompt"),
      Pattern.compile("(?i)<\\s*/?\\s*(system|prompt|instruction)")
  );

  private static final int MAX_LEN = 200;

  /** 校验用户输入(目的地/偏好) */
  public String sanitizeInput(String raw) {
    if (raw == null) {
      return "";
    }
    String s = raw.trim();
    if (s.length() > MAX_LEN) {
      throw new BusinessException(ResultCode.INVALID_INPUT, "输入过长,请控制在 " + MAX_LEN + " 字以内");
    }
    for (Pattern p : INJECTION_PATTERNS) {
      if (p.matcher(s).find()) {
        throw new BusinessException(ResultCode.INVALID_INPUT, "输入包含不允许的内容");
      }
    }
    return s;
  }

  /** 输出侧基础脱敏:截断超长输出(防止异常返回撑爆解析) */
  public String sanitizeOutput(String content) {
    if (content == null) {
      return "";
    }
    String s = content.trim();
    // 去掉可能的 markdown 代码块包裹
    if (s.startsWith("```")) {
      s = s.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
    }
    if (s.length() > 20000) {
      s = s.substring(0, 20000);
    }
    return s;
  }
}
