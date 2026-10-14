package com.tripmate.controller;

import com.tripmate.common.Result;
import com.tripmate.common.BusinessException;
import com.tripmate.common.ResultCode;
import com.tripmate.dto.response.UserInfoVO;
import com.tripmate.service.CaptchaService;
import com.tripmate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户接口:信息 / 改密码 / 改昵称 / 清空行程 / 注销 / 手机号重置密码
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final CaptchaService captchaService;

  /** 当前登录用户信息 */
  @GetMapping("/info")
  public Result<UserInfoVO> info() {
    return Result.ok(userService.currentUser());
  }

  /** 修改密码 */
  @PostMapping("/password")
  public Result<Void> changePassword(@RequestBody Map<String, String> body) {
    String oldPwd = body.get("oldPassword");
    String newPwd = body.get("newPassword");
    String confirm = body.get("confirmPassword");
    if (oldPwd == null || oldPwd.isBlank()) {
      return Result.error(400, "旧密码不能为空");
    }
    if (newPwd == null || newPwd.length() < 6) {
      return Result.error(400, "新密码至少 6 位");
    }
    if (!newPwd.equals(confirm)) {
      return Result.error(400, "两次输入的新密码不一致");
    }
    userService.changePassword(oldPwd, newPwd);
    return Result.ok();
  }

  /** 修改昵称 */
  @PutMapping("/nickname")
  public Result<UserInfoVO> updateNickname(@RequestBody Map<String, String> body) {
    String nick = body.get("nickname");
    if (nick != null && nick.length() > 50) {
      return Result.error(400, "昵称最多 50 字符");
    }
    return Result.ok(userService.updateNickname(nick));
  }

  /** 更新完整资料(昵称/头像/签名) */
  @PutMapping("/profile")
  public Result<UserInfoVO> updateProfile(@RequestBody Map<String, String> body) {
    String nick = body.get("nickname");
    String avatar = body.get("avatar");
    String bio = body.get("bio");
    if (nick != null && nick.length() > 50) return Result.error(400, "昵称最多 50 字符");
    if (bio != null && bio.length() > 200) return Result.error(400, "签名最多 200 字符");
    if (avatar != null && avatar.length() > 255) return Result.error(400, "头像地址过长");
    return Result.ok(userService.updateProfile(nick, avatar, bio));
  }

  /** 清空所有行程 */
  @DeleteMapping("/trips")
  public Result<Void> clearTrips() {
    userService.clearAllTrips();
    return Result.ok();
  }

  /** 注销账号(删自己 + 关联数据) */
  @DeleteMapping
  public Result<Void> deleteAccount() {
    userService.deleteAccount();
    return Result.ok();
  }

  // ========== 公开接口(无需登录) ==========

  /** 手机号重置密码:手机号 + 图形验证码 + 新密码 */
  @PostMapping("/reset-password")
  public Result<Void> resetPassword(@RequestBody Map<String, String> body) {
    String phone = body.get("phone");
    String captcha = body.get("captcha");
    String captchaKey = body.get("captchaKey");
    String newPassword = body.get("newPassword");
    String confirm = body.get("confirmPassword");

    // 校验图形验证码
    captchaService.verifyOrThrow(captchaKey, captcha);
    // 校验手机号格式
    if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "手机号格式不正确");
    }
    // 校验密码
    if (newPassword == null || newPassword.length() < 6) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "新密码至少 6 位");
    }
    if (!newPassword.equals(confirm)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "两次输入的密码不一致");
    }
    userService.resetPasswordByPhone(phone, newPassword);
    return Result.ok();
  }

  /** 校验手机号是否已注册(用于前端提示) */
  @GetMapping("/check-phone")
  public Result<Map<String, Boolean>> checkPhone(@RequestParam String phone) {
    if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
      return Result.ok(Map.of("exists", false));
    }
    Long count = userService.countByPhone(phone);
    return Result.ok(Map.of("exists", count != null && count > 0));
  }
}
