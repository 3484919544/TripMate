package com.tripmate.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.common.BusinessException;
import com.tripmate.common.ResultCode;
import com.tripmate.entity.Trip;
import com.tripmate.entity.TripDay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * AI 结果解析与校验
 * 把大模型返回的 JSON 文本解析为 Trip 实体(含 dayList)
 * 严格校验 Schema,失败抛 AI_FORMAT_ERROR
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiResultParser {

  private final ObjectMapper objectMapper;

  /**
   * 解析 AI 返回内容为 Trip
   *
   * @param content 大模型返回的 JSON 文本
   * @param budget  用户预算(用于校验 totalCost)
   */
  public Trip parse(String content, BigDecimal budget) {
    JsonNode root;
    try {
      root = objectMapper.readTree(content);
    } catch (Exception e) {
      log.warn("AI 返回非合法 JSON: {}", e.getMessage());
      throw new BusinessException(ResultCode.AI_FORMAT_ERROR);
    }
    if (root == null || root.isMissingNode() || !root.isObject()) {
      throw new BusinessException(ResultCode.AI_FORMAT_ERROR);
    }

    try {
      Trip trip = new Trip();
      trip.setTitle(text(root, "title", "未命名行程"));
      trip.setDestination(text(root, "destination", ""));
      trip.setDays(intVal(root, "days", 0));
      trip.setBudget(root.get("budget") != null ? root.get("budget").decimalValue() : budget);
      trip.setTotalCost(root.get("totalCost") != null ? root.get("totalCost").decimalValue() : BigDecimal.ZERO);
      trip.setOverBudget(boolVal(root, "overBudget", false));

      // dayList 解析
      JsonNode daysNode = root.get("dayList");
      if (daysNode == null || !daysNode.isArray()) {
        throw new BusinessException(ResultCode.AI_FORMAT_ERROR);
      }
      List<TripDay> dayList = new ArrayList<>();
      Iterator<JsonNode> it = daysNode.elements();
      while (it.hasNext()) {
        JsonNode d = it.next();
        TripDay td = new TripDay();
        td.setDayIndex(intVal(d, "dayIndex", 0));
        td.setTheme(text(d, "theme", ""));
        td.setMorning(text(d, "morning", ""));
        td.setAfternoon(text(d, "afternoon", ""));
        td.setEvening(text(d, "evening", ""));
        td.setCost(d.get("cost") != null ? d.get("cost").decimalValue() : BigDecimal.ZERO);
        td.setTips(text(d, "tips", ""));
        dayList.add(td);
      }
      trip.setDayList(dayList);

      // 校验:dayList 长度应等于 days
      if (trip.getDays() > 0 && dayList.size() != trip.getDays()) {
        log.warn("AI 返回 dayList 长度 {} 与 days {} 不符", dayList.size(), trip.getDays());
        // 不直接报错,容错:以实际为准,补齐 days
        trip.setDays(dayList.size());
      }

      // 校验预算超支(后端兜底,即使 AI 没标 overBudget 也算一次)
      if (trip.getTotalCost() != null && trip.getBudget() != null
          && trip.getTotalCost().compareTo(trip.getBudget()) > 0) {
        trip.setOverBudget(true);
      }

      return trip;
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.warn("AI 返回结构解析失败: {}", e.getMessage());
      throw new BusinessException(ResultCode.AI_FORMAT_ERROR);
    }
  }

  private String text(JsonNode node, String field, String def) {
    JsonNode n = node.get(field);
    if (n == null || n.isNull()) {
      return def;
    }
    return n.asText();
  }

  private int intVal(JsonNode node, String field, int def) {
    JsonNode n = node.get(field);
    if (n == null || n.isNull()) {
      return def;
    }
    return n.asInt(def);
  }

  private boolean boolVal(JsonNode node, String field, boolean def) {
    JsonNode n = node.get(field);
    if (n == null || n.isNull()) {
      return def;
    }
    return n.asBoolean(def);
  }
}
