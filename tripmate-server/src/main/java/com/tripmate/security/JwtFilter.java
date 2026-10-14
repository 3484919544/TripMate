package com.tripmate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.common.BusinessException;
import com.tripmate.common.Result;
import com.tripmate.common.ResultCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * JWT 鉴权过滤器
 * 放行白名单(/api/auth/register、/api/auth/login)与 OPTIONS 预检
 * 其余 /api/** 要求 Bearer token,解析后放入 UserContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final Set<String> WHITELIST = Set.of(
      "/api/auth/register",
      "/api/auth/login",
      "/api/auth/captcha",
      "/api/uploads/",        // 静态图片资源(头像)
      "/api/user/reset-password",
      "/api/user/check-phone"
  );

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
      throws ServletException, IOException {
    String path = req.getRequestURI();
    String method = req.getMethod();

    // 预检与白名单放行(精确匹配 + 前缀匹配)
    if ("OPTIONS".equalsIgnoreCase(method)
        || WHITELIST.contains(path)
        || WHITELIST.stream().anyMatch(path::startsWith)
        || !path.startsWith("/api/")) {
      chain.doFilter(req, resp);
      return;
    }

    // 解析 token
    String auth = req.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) {
      writeUnauthorized(resp);
      return;
    }
    String token = auth.substring(7);
    try {
      Long userId = jwtUtil.parseUserId(token);
      UserContext.setUserId(userId);
      chain.doFilter(req, resp);
    } catch (BusinessException e) {
      writeUnauthorized(resp);
    } finally {
      UserContext.clear();
    }
  }

  private void writeUnauthorized(HttpServletResponse resp) throws IOException {
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(objectMapper.writeValueAsString(Result.error(ResultCode.UNAUTHORIZED)));
  }
}
