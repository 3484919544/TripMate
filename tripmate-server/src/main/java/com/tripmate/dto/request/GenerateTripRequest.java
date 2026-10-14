package com.tripmate.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 生成行程请求
 * 对应前端 store/trip.js → POST /api/trip/generate
 */
@Data
public class GenerateTripRequest {
  /** 出发地(可空,用于计算交通费用) */
  private String origin;

  @NotBlank(message = "目的地不能为空")
  private String destination;

  @Min(value = 1, message = "天数至少 1 天")
  @Max(value = 30, message = "天数最多 30 天")
  private Integer days;

  @DecimalMin(value = "0.01", message = "预算需大于 0")
  private BigDecimal budget;

  /** 偏好(可空,逗号分隔) */
  private String preference;

  /** 其他想法/备注(可空,纳入行程计算) */
  private String notes;
}
