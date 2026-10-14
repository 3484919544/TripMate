package com.tripmate.service;

import com.tripmate.common.BusinessException;
import com.tripmate.common.ResultCode;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务(内存存储,5分钟过期,定时清理)
 */
@Service
public class CaptchaService {

  private static final ConcurrentHashMap<String, Captcha> STORE = new ConcurrentHashMap<>();
  private static final ScheduledExecutorService CLEANER = Executors.newSingleThreadScheduledExecutor();

  static {
    // 每分钟清理过期验证码
    CLEANER.scheduleAtFixedRate(CaptchaService::cleanExpired, 1, 1, TimeUnit.MINUTES);
  }

  private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final long TTL_MS = 5 * 60 * 1000L; // 5 分钟

  public record Captcha(String code, long expireAt) {}

  /** 生成验证码,返回 captchaKey + base64 图片 */
  public CaptchaResult generate() {
    String key = UUID.randomUUID().toString().replace("-", "");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      sb.append(CHARS.charAt((int) (Math.random() * CHARS.length())));
    }
    String code = sb.toString();
    STORE.put(key, new Captcha(code, System.currentTimeMillis() + TTL_MS));
    String image = buildBase64Image(code);
    return new CaptchaResult(key, image);
  }

  /** 校验验证码(忽略大小写,校验后立即删除,一次有效) */
  public boolean verify(String key, String userInput) {
    Captcha c = STORE.remove(key);
    if (c == null) return false;
    if (System.currentTimeMillis() > c.expireAt()) return false;
    return c.code.equalsIgnoreCase(userInput);
  }

  /** 校验并抛业务异常 */
  public void verifyOrThrow(String key, String userInput) {
    if (!verify(key, userInput)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
    }
  }

  private static void cleanExpired() {
    long now = System.currentTimeMillis();
    STORE.entrySet().removeIf(e -> now > e.getValue().expireAt);
  }

  /** 生成简单的 SVG 验证码图(转 base64),无需额外依赖 */
  private String buildBase64Image(String code) {
    StringBuilder sb = new StringBuilder();
    sb.append("<svg xmlns='http://www.w3.org/2000/svg' width='110' height='40' viewBox='0 0 110 40'>");
    sb.append("<rect width='110' height='40' fill='#f0f2f5'/>");
    // 干扰线
    for (int i = 0; i < 4; i++) {
      int x1 = (int) (Math.random() * 110);
      int y1 = (int) (Math.random() * 40);
      int x2 = (int) (Math.random() * 110);
      int y2 = (int) (Math.random() * 40);
      sb.append("<line x1='").append(x1).append("' y1='").append(y1)
          .append("' x2='").append(x2).append("' y2='").append(y2)
          .append("' stroke='#ccc' stroke-width='1'/>");
    }
    // 干扰点
    for (int i = 0; i < 20; i++) {
      int cx = (int) (Math.random() * 110);
      int cy = (int) (Math.random() * 40);
      sb.append("<circle cx='").append(cx).append("' cy='").append(cy)
          .append("' r='1' fill='#aaa'/>");
    }
    // 字符
    for (int i = 0; i < code.length(); i++) {
      int x = 15 + i * 22 + (int) (Math.random() * 6 - 3);
      int y = 28 + (int) (Math.random() * 6 - 3);
      int rot = (int) (Math.random() * 40 - 20);
      String color = String.format("#%02x%02x%02x",
          30 + (int) (Math.random() * 80),
          30 + (int) (Math.random() * 80),
          100 + (int) (Math.random() * 100));
      sb.append("<text x='").append(x).append("' y='").append(y)
          .append("' font-size='22' font-family='Arial' font-weight='bold'")
          .append(" fill='").append(color).append("' transform='rotate(").append(rot)
          .append(" ").append(x).append(" ").append(y).append(")'>")
          .append(code.charAt(i)).append("</text>");
    }
    sb.append("</svg>");
    return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(sb.toString().getBytes());
  }

  public record CaptchaResult(String captchaKey, String captchaImage) {}
}
