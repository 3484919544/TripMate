package com.tripmate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("trip_day")
public class TripDay {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long tripId;
  private Integer dayIndex;
  private String theme;
  private String morning;
  private String afternoon;
  private String evening;
  private BigDecimal cost;
  private String tips;
}
