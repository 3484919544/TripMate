package com.tripmate.controller;

import com.tripmate.common.PageResult;
import com.tripmate.common.Result;
import com.tripmate.dto.request.AdminCreateUserRequest;
import com.tripmate.dto.request.AdminUpdateUserRequest;
import com.tripmate.dto.response.UserInfoVO;
import com.tripmate.entity.AiLog;
import com.tripmate.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理类接口:AI 调用记录(当前用户) + 用户管理(仅管理员)
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  // ---------- AI 日志 ----------

  @GetMapping("/ai-logs")
  public Result<PageResult<AiLog>> aiLogs(@RequestParam(required = false) Integer pageNum,
                                           @RequestParam(required = false) Integer pageSize) {
    return Result.ok(adminService.listAiLogs(pageNum, pageSize));
  }

  // ---------- 用户管理(仅管理员) ----------

  /** 管理员创建用户(可创建管理员或普通用户) */
  @PostMapping("/user")
  public Result<UserInfoVO> createUser(@Valid @RequestBody AdminCreateUserRequest req) {
    return Result.ok(adminService.createUser(req));
  }

  /** 用户列表(按用户名/昵称搜索) */
  @GetMapping("/user/list")
  public Result<PageResult<UserInfoVO>> listUsers(@RequestParam(required = false) Integer pageNum,
                                                   @RequestParam(required = false) Integer pageSize,
                                                   @RequestParam(required = false) String keyword) {
    return Result.ok(adminService.listUsers(pageNum, pageSize, keyword));
  }

  /** 修改用户(重置密码/昵称/角色) */
  @PutMapping("/user/{id}")
  public Result<UserInfoVO> updateUser(@PathVariable Long id,
                                       @Valid @RequestBody AdminUpdateUserRequest req) {
    return Result.ok(adminService.updateUser(id, req));
  }

  /** 删除用户 */
  @DeleteMapping("/user/{id}")
  public Result<Void> deleteUser(@PathVariable Long id) {
    adminService.deleteUser(id);
    return Result.ok();
  }
}
