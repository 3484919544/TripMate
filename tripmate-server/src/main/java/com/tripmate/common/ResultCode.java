package com.tripmate.common;

/**
 * 统一返回码
 * 200 成功;400 参数错误;401 未登录;500 服务器错误
 * 1001~1003 AI 相关业务码(与前端契约对齐)
 */
public enum ResultCode {

  SUCCESS(200, "操作成功"),
  BAD_REQUEST(400, "请求参数错误"),
  UNAUTHORIZED(401, "未登录或登录已过期"),
  FORBIDDEN(403, "无权限"),
  NOT_FOUND(404, "资源不存在"),
  SERVER_ERROR(500, "服务器内部错误"),

  // AI 业务码(与前端 request.js 错误码一致)
  AI_CALL_FAILED(1001, "AI 调用失败,请稍后重试"),
  AI_FORMAT_ERROR(1002, "AI 返回格式错误,请重试"),
  OVER_BUDGET(1003, "生成结果超预算,请调整"),
  INVALID_INPUT(1004, "输入不合法,请检查");

  private final int code;
  private final String message;

  ResultCode(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
