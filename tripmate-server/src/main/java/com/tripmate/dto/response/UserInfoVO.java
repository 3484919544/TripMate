package com.tripmate.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息视图(不含密码)
 */
@Data
public class UserInfoVO {
  private Long id;
  /** 10位数字账号 */
  private String account;
  private String username;
  private String nickname;
  private String avatar;
  private String bio;
  /** 角色:USER / ADMIN,前端据此显示管理员入口 */
  private String role;
  private LocalDateTime createTime;
}
