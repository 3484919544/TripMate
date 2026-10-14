package com.tripmate.dto.response;

import lombok.Data;

/**
 * 登录返回:{ token, user }
 * 前端 store/user.js 读取 token 与 user
 */
@Data
public class LoginResponse {
  private String token;
  private UserInfoVO user;

  public LoginResponse(String token, UserInfoVO user) {
    this.token = token;
    this.user = user;
  }
}
