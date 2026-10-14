package com.tripmate.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tripmate.entity.User;
import com.tripmate.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时数据种子:
 * 1. 若系统中无任何管理员,自动创建默认管理员(分配10位数字账号)
 * 2. 给已有用户补齐 account(如果为 null)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

  private final UserMapper userMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Value("${app.admin.username:admin}")
  private String adminUsername;

  @Value("${app.admin.password:}")
  private String adminPassword;

  @Override
  public void run(ApplicationArguments args) {
    // 给已有用户补 account
    var allUsers = userMapper.selectList(null);
    for (User u : allUsers) {
      if (u.getAccount() == null || u.getAccount().isBlank()) {
        u.setAccount(generateUniqueAccount());
        userMapper.updateById(u);
        log.info("为用户 {} 补齐账号 {}", u.getUsername(), u.getAccount());
      }
    }

    // 创建默认管理员:密码必须通过 app.admin.password(APP_ADMIN_PASSWORD)配置,
    // 未配置则跳过,避免仓库内置已知默认口令
    if (adminPassword == null || adminPassword.isBlank()) {
      log.warn("未配置 app.admin.password(APP_ADMIN_PASSWORD),跳过默认管理员初始化");
      return;
    }
    Long adminCount = userMapper.selectCount(
        new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
    if (adminCount != null && adminCount > 0) {
      return;
    }
    User admin = new User();
    admin.setUsername(adminUsername);
    admin.setPassword(passwordEncoder.encode(adminPassword));
    admin.setNickname("系统管理员");
    admin.setRole("ADMIN");
    admin.setAccount(generateUniqueAccount());
    try {
      userMapper.insert(admin);
      log.warn("已自动创建默认管理员 → 账号: {}(密码来自 APP_ADMIN_PASSWORD)", admin.getAccount());
    } catch (Exception e) {
      log.warn("默认管理员创建失败: {}", e.getMessage());
    }
  }

  private String generateUniqueAccount() {
    for (int i = 0; i < 20; i++) {
      String acc = String.valueOf(1_000_000_000L + (long) (Math.random() * 9_000_000_000L));
      Long exists = userMapper.selectCount(
          new LambdaQueryWrapper<User>().eq(User::getAccount, acc));
      if (exists == null || exists == 0) return acc;
    }
    return String.valueOf(System.currentTimeMillis()).substring(0, 10);
  }
}
