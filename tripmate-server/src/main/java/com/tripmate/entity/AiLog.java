package com.tripmate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_log")
public class AiLog {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private String prompt;
  private String model;
  private LocalDateTime requestTime;
  private LocalDateTime responseTime;
  private String status;
  private String errorMsg;
  private Integer tokens;
}
