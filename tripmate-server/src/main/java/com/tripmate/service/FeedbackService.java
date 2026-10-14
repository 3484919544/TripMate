package com.tripmate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tripmate.common.BusinessException;
import com.tripmate.common.ResultCode;
import com.tripmate.dto.request.FeedbackRequest;
import com.tripmate.entity.Feedback;
import com.tripmate.entity.User;
import com.tripmate.mapper.FeedbackMapper;
import com.tripmate.mapper.UserMapper;
import com.tripmate.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

  private final FeedbackMapper feedbackMapper;
  private final UserMapper userMapper;

  /** 提交反馈 */
  public Long create(FeedbackRequest req) {
    Long userId = UserContext.getUserId();
    Feedback fb = new Feedback();
    fb.setUserId(userId);
    fb.setTripId(req.getTripId());
    fb.setContent(req.getContent());
    fb.setStatus("PENDING");
    feedbackMapper.insert(fb);
    return fb.getId();
  }

  /** 当前用户的反馈列表(带回复) */
  public List<Feedback> myList() {
    Long userId = UserContext.getUserId();
    return feedbackMapper.selectList(
        new LambdaQueryWrapper<Feedback>()
            .eq(Feedback::getUserId, userId)
            .orderByDesc(Feedback::getCreateTime));
  }

  /** 管理员:全部反馈列表 */
  public List<Feedback> adminList() {
    requireAdmin();
    return feedbackMapper.selectList(
        new LambdaQueryWrapper<Feedback>()
            .orderByDesc(Feedback::getCreateTime));
  }

  /** 管理员:回复 + 标记已处理 */
  public void reply(Long id, String reply) {
    requireAdmin();
    Feedback fb = feedbackMapper.selectById(id);
    if (fb == null) throw new BusinessException(ResultCode.NOT_FOUND, "反馈不存在");
    if (reply == null || reply.isBlank()) throw new BusinessException(ResultCode.BAD_REQUEST, "回复内容不能为空");
    fb.setReply(reply.trim());
    fb.setReplyTime(LocalDateTime.now());
    fb.setStatus("RESOLVED");
    feedbackMapper.updateById(fb);
  }

  /** 管理员:删除反馈 */
  public void delete(Long id) {
    requireAdmin();
    feedbackMapper.deleteById(id);
  }

  /** 管理员权限校验,否则抛 403 */
  public void requireAdmin() {
    Long uid = UserContext.getUserId();
    User u = userMapper.selectById(uid);
    if (u == null || !"ADMIN".equals(u.getRole())) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权操作,仅管理员可处理反馈");
    }
  }
}
