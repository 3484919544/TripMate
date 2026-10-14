package com.tripmate.security;

import com.tripmate.common.BusinessException;
import com.tripmate.common.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

/**
 * JWT 工具:生成/校验 token
 */
@Slf4j
@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expire}")
  private long expire;

  private SecretKey key;

  @PostConstruct
  public void init() {
    byte[] bytes;
    if (secret == null || secret.isBlank()) {
      // 未配置密钥时按启动生成随机密钥,避免使用内置默认密钥;重启后已签发 token 失效
      bytes = new byte[32];
      new SecureRandom().nextBytes(bytes);
      log.warn("未配置 jwt.secret(JWT_SECRET),已生成随机密钥,重启后所有 token 将失效;生产环境请配置固定的 JWT_SECRET");
    } else {
      // 密钥不足 32 字节时补齐,避免 Keys 抛异常
      bytes = secret.getBytes(StandardCharsets.UTF_8);
      if (bytes.length < 32) {
        byte[] padded = new byte[32];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        bytes = padded;
      }
    }
    this.key = Keys.hmacShaKeyFor(bytes);
  }

  /** 生成 token */
  public String generate(Long userId, String username) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + expire);
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("username", username)
        .issuedAt(now)
        .expiration(exp)
        .signWith(key)
        .compact();
  }

  /** 解析并校验,失败抛 401 */
  public Long parseUserId(String token) {
    try {
      Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      return Long.valueOf(c.getSubject());
    } catch (Exception e) {
      log.debug("JWT 校验失败: {}", e.getMessage());
      throw new BusinessException(ResultCode.UNAUTHORIZED);
    }
  }
}
