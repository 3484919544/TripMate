package com.tripmate.common;

import lombok.Getter;

/**
 * 业务异常:抛出后由全局处理器转为 Result.error
 */
@Getter
public class BusinessException extends RuntimeException {
  private final int code;

  public BusinessException(ResultCode rc) {
    super(rc.getMessage());
    this.code = rc.getCode();
  }

  public BusinessException(ResultCode rc, String message) {
    super(message);
    this.code = rc.getCode();
  }

  public BusinessException(int code, String message) {
    super(message);
    this.code = code;
  }
}
