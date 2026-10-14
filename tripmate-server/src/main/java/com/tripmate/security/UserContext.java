package com.tripmate.security;

/**
 * 当前登录用户上下文(ThreadLocal)
 * JwtFilter 解析后放入,service 直接取,免传参
 */
public class UserContext {

  private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

  public static void setUserId(Long userId) {
    CURRENT.set(userId);
  }

  public static Long getUserId() {
    Long id = CURRENT.get();
    if (id == null) {
      throw new com.tripmate.common.BusinessException(com.tripmate.common.ResultCode.UNAUTHORIZED);
    }
    return id;
  }

  /** 可空版本:用于可选用户场景 */
  public static Long getUserIdOrNull() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }
}
