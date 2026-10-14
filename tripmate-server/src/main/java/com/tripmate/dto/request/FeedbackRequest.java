package com.tripmate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {
  @NotBlank(message = "反馈内容不能为空")
  @Size(max = 1000, message = "反馈内容不超过 1000 字")
  private String content;

  /** 关联行程 ID(可选) */
  private Long tripId;
}
