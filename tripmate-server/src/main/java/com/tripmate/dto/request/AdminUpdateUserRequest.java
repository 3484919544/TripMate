package com.tripmate.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员修改用户请求
 * 密码为空表示不改密码;其余字段为空表示不改
 */
@Data
public class AdminUpdateUserRequest {
  /** 新密码(空则不改) */
  @Size(min = 6, max = 30, message = "密码 6-30 个字符")
  private String password;

  @Size(max = 50)
  private String nickname;

  /** 角色:USER / ADMIN(空则不改) */
  private String role;
}
