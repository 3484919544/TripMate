package com.tripmate.service;

import com.tripmate.common.BusinessException;
import com.tripmate.common.ResultCode;
import com.tripmate.dto.response.UserInfoVO;
import com.tripmate.entity.AiLog;
import com.tripmate.entity.Trip;
import com.tripmate.entity.TripDay;
import com.tripmate.entity.User;
import com.tripmate.mapper.AiLogMapper;
import com.tripmate.mapper.TripDayMapper;
import com.tripmate.mapper.TripMapper;
import com.tripmate.mapper.UserMapper;
import com.tripmate.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserMapper userMapper;
  private final TripMapper tripMapper;
  private final TripDayMapper tripDayMapper;
  private final AiLogMapper aiLogMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  /** 当前登录用户信息 */
  public UserInfoVO currentUser() {
    Long uid = UserContext.getUserId();
    User u = userMapper.selectById(uid);
    if (u == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }
    return toVO(u);
  }

  /** 修改密码:校验旧密码,加密新密码 */
  public void changePassword(String oldPwd, String newPwd) {
    Long uid = UserContext.getUserId();
    User u = userMapper.selectById(uid);
    if (u == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }
    if (!passwordEncoder.matches(oldPwd, u.getPassword())) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "旧密码错误");
    }
    u.setPassword(passwordEncoder.encode(newPwd));
    userMapper.updateById(u);
  }

  /** 修改昵称 */
  public UserInfoVO updateNickname(String nickname) {
    return updateProfile(nickname, null, null);
  }

  /** 更新完整资料(昵称/头像/签名),null 表示不改 */
  public UserInfoVO updateProfile(String nickname, String avatar, String bio) {
    Long uid = UserContext.getUserId();
    User u = userMapper.selectById(uid);
    if (u == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }
    if (nickname != null) {
      u.setNickname(nickname.trim().isEmpty() ? null : nickname.trim());
    }
    if (avatar != null) {
      u.setAvatar(avatar.trim().isEmpty() ? null : avatar.trim());
    }
    if (bio != null) {
      u.setBio(bio.trim().isEmpty() ? null : bio.trim());
    }
    userMapper.updateById(u);
    return toVO(u);
  }

  /** 清空当前用户所有行程(级联删 trip_day) */
  @Transactional(rollbackFor = Exception.class)
  public void clearAllTrips() {
    Long uid = UserContext.getUserId();
    List<Trip> trips = tripMapper.selectList(
        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Trip>()
            .eq(Trip::getUserId, uid));
    for (Trip t : trips) {
      tripDayMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TripDay>()
          .eq(TripDay::getTripId, t.getId()));
    }
    tripMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Trip>()
        .eq(Trip::getUserId, uid));
  }

  /** 注销账号:删自己 + 关联行程 + 每日安排 + AI 日志 */
  @Transactional(rollbackFor = Exception.class)
  public void deleteAccount() {
    Long uid = UserContext.getUserId();
    // 不能删管理员(避免系统无管理员)
    User u = userMapper.selectById(uid);
    if (u != null && "ADMIN".equals(u.getRole())) {
      Long adminCount = userMapper.selectCount(
          new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
              .eq(User::getRole, "ADMIN"));
      if (adminCount != null && adminCount <= 1) {
        throw new BusinessException(ResultCode.FORBIDDEN, "系统至少保留一个管理员账号");
      }
    }
    // 删行程+每日
    clearAllTrips();
    // 删 AI 日志
    aiLogMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiLog>()
        .eq(AiLog::getUserId, uid));
    // 删用户
    userMapper.deleteById(uid);
  }

  /** 根据手机号重置密码(不需要登录) */
  public void resetPasswordByPhone(String phone, String newPassword) {
    User u = userMapper.selectOne(
        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
            .eq(User::getPhone, phone));
    if (u == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "该手机号未注册");
    }
    u.setPassword(passwordEncoder.encode(newPassword));
    userMapper.updateById(u);
  }

  /** 统计某手机号是否已注册(公开接口用) */
  public Long countByPhone(String phone) {
    return userMapper.selectCount(
        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
            .eq(User::getPhone, phone));
  }

  private UserInfoVO toVO(User u) {
    UserInfoVO vo = new UserInfoVO();
    vo.setId(u.getId());
    vo.setAccount(u.getAccount());
    vo.setUsername(u.getUsername());
    vo.setNickname(u.getNickname());
    vo.setAvatar(u.getAvatar());
    vo.setBio(u.getBio());
    vo.setRole(u.getRole());
    vo.setCreateTime(u.getCreateTime());
    return vo;
  }
}
