package com.tripmate.common;

import lombok.Data;

/**
 * 统一返回包装 { code, message, data }
 * 与前端 request.js 契约一致:code=200 走 data,否则走 message
 */
@Data
public class Result<T> {
  private int code;
  private String message;
  private T data;

  public static <T> Result<T> ok() {
    return ok(null);
  }

  public static <T> Result<T> ok(T data) {
    Result<T> r = new Result<>();
    r.code = ResultCode.SUCCESS.getCode();
    r.message = ResultCode.SUCCESS.getMessage();
    r.data = data;
    return r;
  }

  public static <T> Result<T> error(ResultCode rc) {
    return error(rc.getCode(), rc.getMessage());
  }

  public static <T> Result<T> error(int code, String message) {
    Result<T> r = new Result<>();
    r.code = code;
    r.message = message;
    return r;
  }
}
