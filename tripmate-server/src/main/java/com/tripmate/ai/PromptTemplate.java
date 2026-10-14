package com.tripmate.ai;

import com.tripmate.dto.request.GenerateTripRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 提示词工程:组装系统提示 + 用户提示
 * 强制 AI 输出固定 JSON Schema,降低格式容错压力
 */
@Component
public class PromptTemplate {

  /** 系统提示词:角色 + 任务 + 输出格式约束 */
  public static final String SYSTEM_PROMPT = """
      你是一名资深旅游行程规划师。请根据用户输入的出发地、目的地、天数、预算、偏好,
      生成一份结构化的旅游行程安排。

      【交通费用计算规则】
      - 如果用户提供了出发地,请根据出发地到目的地的大致距离估算往返交通费用(机票/高铁/长途大巴等,取主流方式中间价)。
      - 交通费用应计入 totalCost 的第一天或最后一天 cost 中,或作为单独条目。
      - 出发地与目的地相同时,交通费用记为 0。

      【输出要求】
      1. 只输出一个 JSON 对象,不要输出任何解释性文字、markdown 代码块标记或多余换行。
      2. JSON Schema 如下(字段名、类型、嵌套结构必须严格一致):
      {
        "title": "行程标题(字符串)",
        "origin": "出发地(字符串,若用户未提供则为空字符串)",
        "destination": "目的地(字符串)",
        "days": 天数(整数),
        "budget": 预算(数字),
        "totalCost": 预计总花费(数字,应尽量不超过 budget),
        "overBudget": 是否超预算(布尔值),
        "dayList": [
          {
            "dayIndex": 第几天(整数,从1开始),
            "theme": "当日主题(字符串)",
            "morning": "上午安排(字符串,2-4个具体景点或活动)",
            "afternoon": "下午安排(字符串)",
            "evening": "晚上安排(字符串)",
            "cost": "当日花费(数字)",
            "tips": "当日提示(字符串,交通/餐饮/注意事项)"
          }
        ]
      }
      3. dayList 长度必须等于 days;dayIndex 从 1 连续递增。
      4. 所有安排要具体、可执行,贴合偏好与预算。如总花费超过预算,overBudget 设为 true。
      5. 交通费用(如有)应显式体现在某一天的 cost 和 tips 中,让用户清楚钱花在哪里。
      """;

  /** 组装用户提示词 */
  public String buildUserPrompt(GenerateTripRequest req) {
    StringBuilder sb = new StringBuilder();
    if (req.getOrigin() != null && !req.getOrigin().isBlank()) {
      sb.append("出发地:").append(req.getOrigin()).append("\n");
    }
    sb.append("目的地:").append(req.getDestination()).append("\n");
    sb.append("天数:").append(req.getDays()).append(" 天\n");
    sb.append("预算:").append(req.getBudget()).append(" 元\n");
    sb.append("偏好:");
    if (req.getPreference() != null && !req.getPreference().isBlank()) {
      sb.append(req.getPreference());
    } else {
      sb.append("无特殊要求");
    }
    sb.append("\n");
    if (req.getNotes() != null && !req.getNotes().isBlank()) {
      sb.append("其他要求:").append(req.getNotes()).append("\n");
    }
    sb.append("\n请生成行程,严格遵守系统提示中的 JSON Schema。");
    return sb.toString();
  }

  /** 给 AI 的"原始输入"文本(写入 ai_log 的 prompt 字段) */
  public String buildLogPrompt(GenerateTripRequest req) {
    BigDecimal budget = req.getBudget();
    return (req.getOrigin() != null ? "出发地=" + req.getOrigin() + "|" : "")
        + "目的地=" + req.getDestination()
        + "|天数=" + req.getDays()
        + "|预算=" + budget
        + "|偏好=" + req.getPreference()
        + (req.getNotes() != null && !req.getNotes().isBlank() ? "|备注=" + req.getNotes() : "");
  }
}
