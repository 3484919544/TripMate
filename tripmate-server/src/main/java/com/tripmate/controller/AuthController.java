package com.tripmate.controller;

import com.tripmate.common.Result;
import com.tripmate.dto.request.LoginRequest;
import com.tripmate.dto.request.RegisterRequest;
import com.tripmate.dto.response.LoginResponse;
import com.tripmate.service.AuthService;
import com.tripmate.service.CaptchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final CaptchaService captchaService;

  /** 获取图形验证码 */
  @GetMapping("/api/auth/captcha")
  public Result<Map<String, String>> captcha() {
    CaptchaService.CaptchaResult r = captchaService.generate();
    Map<String, String> m = new HashMap<>();
    m.put("captchaKey", r.captchaKey());
    m.put("captchaImage", r.captchaImage());
    return Result.ok(m);
  }

  /** 注册(返回账号) */
  @PostMapping("/api/auth/register")
  public Result<AuthService.RegisterResult> register(@Valid @RequestBody RegisterRequest req) {
    return Result.ok(authService.register(req));
  }

  /** 登录(账号 + 密码 + 验证码) */
  @PostMapping("/api/auth/login")
  public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
    return Result.ok(authService.login(req));
  }
}
