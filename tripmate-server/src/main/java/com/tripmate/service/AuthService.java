package com.tripmate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tripmate.common.BusinessException;
import com.tripmate.common.ResultCode;
import com.tripmate.dto.request.LoginRequest;
import com.tripmate.dto.request.RegisterRequest;
import com.tripmate.dto.response.LoginResponse;
import com.tripmate.dto.response.UserInfoVO;
import com.tripmate.entity.User;
import com.tripmate.mapper.UserMapper;
import com.tripmate.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserMapper userMapper;
  private final JwtUtil jwtUtil;
  private final CaptchaService captchaService;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  /** 注册(返回含账号信息) */
  public RegisterResult register(RegisterRequest req) {
    // 基础校验
    if (!req.getPassword().equals(req.getConfirmPassword())) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码输入不一致");
    }
    // 用户名唯一
    Long existsUser = userMapper.selectCount(
        new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
    if (existsUser != null && existsUser > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
    }
    // 手机号唯一
    Long existsPhone = userMapper.selectCount(
        new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
    if (existsPhone != null && existsPhone > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "手机号已被注册");
    }

    User user = new User();
    user.setPhone(req.getPhone());
    user.setUsername(req.getUsername());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setNickname(req.getUsername());
    user.setRole("USER");
    // 自动生成 10 位唯一数字账号
    user.setAccount(generateUniqueAccount());

    userMapper.insert(user);
    return new RegisterResult(user.getAccount(), user.getUsername());
  }

  /** 登录(账号 + 密码 + 验证码) */
  public LoginResponse login(LoginRequest req) {
    // 先校验验证码
    captchaService.verifyOrThrow(req.getCaptchaKey(), req.getCaptcha());

    User user = userMapper.selectOne(
        new LambdaQueryWrapper<User>().eq(User::getAccount, req.getAccount()));
    if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "账号或密码错误");
    }
    String token = jwtUtil.generate(user.getId(), user.getUsername());
    return new LoginResponse(token, toVO(user));
  }

  /** 生成 10 位唯一数字账号(最多重试 10 次) */
  private String generateUniqueAccount() {
    for (int i = 0; i < 10; i++) {
      String acc = String.valueOf(1_000_000_000L + (long) (Math.random() * 9_000_000_000L));
      Long exists = userMapper.selectCount(
          new LambdaQueryWrapper<User>().eq(User::getAccount, acc));
      if (exists == null || exists == 0) return acc;
    }
    // 兜底:基于时间戳,保证唯一
    String acc = String.valueOf(System.currentTimeMillis()).substring(0, 10);
    return acc;
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

  public record RegisterResult(String account, String username) {}
}
