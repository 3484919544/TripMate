package com.tripmate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class Feedback {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private Long tripId;
  private String content;
  /** PENDING 待处理 / RESOLVED 已处理 */
  private String status;
  private String reply;
  private LocalDateTime replyTime;
  private LocalDateTime createTime;
}
