package com.tripmate.controller;

import com.tripmate.common.Result;
import com.tripmate.dto.request.FeedbackRequest;
import com.tripmate.entity.Feedback;
import com.tripmate.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 反馈接口
 * 用户:提交反馈 / 查看自己的反馈(含管理员回复)
 * 管理员:全部反馈列表 / 回复 / 删除
 */
@RestController
@RequiredArgsConstructor
public class FeedbackController {

  private final FeedbackService feedbackService;

  // ========== 用户接口 ==========

  /** 提交反馈 */
  @PostMapping("/api/feedback")
  public Result<Map<String, Object>> create(@Valid @RequestBody FeedbackRequest req) {
    Long id = feedbackService.create(req);
    return Result.ok(Map.of("id", id));
  }

  /** 我的反馈(含管理员回复) */
  @GetMapping("/api/feedback/my")
  public Result<List<Feedback>> myList() {
    return Result.ok(feedbackService.myList());
  }

  // ========== 管理员接口 ==========

  /** 全部反馈(管理员) */
  @GetMapping("/api/admin/feedback/list")
  public Result<List<Feedback>> adminList() {
    return Result.ok(feedbackService.adminList());
  }

  /** 管理员回复反馈(同时标记已处理) */
  @PutMapping("/api/admin/feedback/{id}/reply")
  public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
    feedbackService.reply(id, body.get("reply"));
    return Result.ok();
  }

  /** 管理员删除反馈 */
  @DeleteMapping("/api/admin/feedback/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    feedbackService.delete(id);
    return Result.ok();
  }
}
