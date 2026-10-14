package com.tripmate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 行程实体
 * 注意:dayList 为非数据库字段(前端契约用 dayList 承载每日安排)
 */
@Data
@TableName("trip")
public class Trip {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private String title;
  private String origin;       // 出发地
  private String destination;
  private Integer days;
  private BigDecimal budget;
  private String preference;
  private BigDecimal totalCost;
  private Boolean overBudget;
  private String status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;

  /** 每日安排(非持久化,仅传输) */
  @TableField(exist = false)
  private List<TripDay> dayList;
}
