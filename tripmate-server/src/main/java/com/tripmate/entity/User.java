package com.tripmate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
  @TableId(type = IdType.AUTO)
  private Long id;
  /** 10位数字账号,系统自动生成 */
  private String account;
  private String phone;
  private String username;
  private String password;
  private String nickname;
  private String avatar;
  private String bio;
  /** 角色:USER / ADMIN */
  private String role;
  @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
  private LocalDateTime createTime;
}
