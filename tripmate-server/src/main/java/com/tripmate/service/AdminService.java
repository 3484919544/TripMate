package com.tripmate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tripmate.common.BusinessException;
import com.tripmate.common.PageResult;
import com.tripmate.common.ResultCode;
import com.tripmate.dto.request.AdminCreateUserRequest;
import com.tripmate.dto.request.AdminUpdateUserRequest;
import com.tripmate.dto.response.UserInfoVO;
import com.tripmate.entity.AiLog;
import com.tripmate.entity.User;
import com.tripmate.mapper.AiLogMapper;
import com.tripmate.mapper.UserMapper;
import com.tripmate.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理类服务:AI 调用记录 + 用户管理(仅管理员)
 */
@Service
@RequiredArgsConstructor
public class AdminService {

  private final AiLogMapper aiLogMapper;
  private final UserMapper userMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  // -------------------- AI 日志(当前用户) --------------------

  public PageResult<AiLog> listAiLogs(Integer pageNum, Integer pageSize) {
    Long userId = UserContext.getUserId();
    int num = pageNum == null || pageNum < 1 ? 1 : pageNum;
    int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);

    Page<AiLog> page = aiLogMapper.selectPage(new Page<>(num, size),
        new LambdaQueryWrapper<AiLog>()
            .eq(AiLog::getUserId, userId)
            .orderByDesc(AiLog::getRequestTime));
    return new PageResult<>(page.getRecords(), page.getTotal());
  }

  // -------------------- 用户管理(仅管理员) --------------------

  /** 校验当前用户是管理员,返回管理员实体 */
  private User requireAdmin() {
    Long uid = UserContext.getUserId();
    User u = userMapper.selectById(uid);
    if (u == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }
    if (!"ADMIN".equals(u.getRole())) {
      throw new BusinessException(ResultCode.FORBIDDEN, "需要管理员权限");
    }
    return u;
  }

  /** 管理员创建用户(可指定角色,默认 USER;即"管理员由管理员注册") */
  public UserInfoVO createUser(AdminCreateUserRequest req) {
    requireAdmin();
    Long exists = userMapper.selectCount(
        new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
    if (exists != null && exists > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
    }
    User u = new User();
    u.setUsername(req.getUsername());
    u.setPassword(passwordEncoder.encode(req.getPassword()));
    u.setNickname(req.getNickname() != null && !req.getNickname().isBlank()
        ? req.getNickname() : req.getUsername());
    String role = (req.getRole() == null || req.getRole().isBlank()) ? "USER" : req.getRole();
    if (!"USER".equals(role) && !"ADMIN".equals(role)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "角色只能为 USER 或 ADMIN");
    }
    u.setRole(role);
    userMapper.insert(u);
    return toVO(u);
  }

  /** 用户列表(支持按用户名/昵称搜索) */
  public PageResult<UserInfoVO> listUsers(Integer pageNum, Integer pageSize, String keyword) {
    requireAdmin();
    int num = pageNum == null || pageNum < 1 ? 1 : pageNum;
    int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);

    LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
    if (keyword != null && !keyword.isBlank()) {
      String kw = keyword.trim();
      w.and(q -> q.like(User::getUsername, kw).or().like(User::getNickname, kw));
    }
    w.orderByDesc(User::getCreateTime);
    Page<User> page = userMapper.selectPage(new Page<>(num, size), w);
    return new PageResult<>(
        page.getRecords().stream().map(this::toVO).toList(),
        page.getTotal());
  }

  /** 管理员修改用户(重置密码/昵称/角色) */
  public UserInfoVO updateUser(Long id, AdminUpdateUserRequest req) {
    requireAdmin();
    User u = userMapper.selectById(id);
    if (u == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }
    if (req.getPassword() != null && !req.getPassword().isBlank()) {
      u.setPassword(passwordEncoder.encode(req.getPassword()));
    }
    if (req.getNickname() != null && !req.getNickname().isBlank()) {
      u.setNickname(req.getNickname());
    }
    if (req.getRole() != null && !req.getRole().isBlank()) {
      if (!"USER".equals(req.getRole()) && !"ADMIN".equals(req.getRole())) {
        throw new BusinessException(ResultCode.BAD_REQUEST, "角色只能为 USER 或 ADMIN");
      }
      u.setRole(req.getRole());
    }
    // 仅更新非空字段:密码字段为已加密的新值或原值,统一 updateById
    userMapper.updateById(u);
    return toVO(u);
  }

  /** 管理员删除用户(不能删自己) */
  public void deleteUser(Long id) {
    User admin = requireAdmin();
    if (id.equals(admin.getId())) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "不能删除自己");
    }
    User u = userMapper.selectById(id);
    if (u == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }
    userMapper.deleteById(id);
  }

  private UserInfoVO toVO(User u) {
    UserInfoVO vo = new UserInfoVO();
    vo.setId(u.getId());
    vo.setAccount(u.getAccount());
    vo.setUsername(u.getUsername());
    vo.setNickname(u.getNickname());
    vo.setRole(u.getRole());
    vo.setCreateTime(u.getCreateTime());
    return vo;
  }
}
