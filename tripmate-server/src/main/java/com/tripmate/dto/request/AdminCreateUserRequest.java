package com.tripmate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员创建用户请求(可指定角色)
 * 仅管理员可调用 /api/admin/user
 */
@Data
public class AdminCreateUserRequest {
  @NotBlank(message = "用户名不能为空")
  @Size(min = 3, max = 20, message = "用户名 3-20 个字符")
  private String username;

  @NotBlank(message = "密码不能为空")
  @Size(min = 6, max = 30, message = "密码 6-30 个字符")
  private String password;

  @Size(max = 50)
  private String nickname;

  /** 角色:USER / ADMIN,不填默认 USER */
  private String role;
}
