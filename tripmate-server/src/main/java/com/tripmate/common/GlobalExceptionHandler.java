package com.tripmate.common;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理:统一转 Result
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 业务异常 */
  @ExceptionHandler(BusinessException.class)
  public Result<Void> handleBusiness(BusinessException e) {
    log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
    return Result.error(e.getCode(), e.getMessage());
  }

  /** 参数校验异常(@Valid) */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Result<Void> handleValid(MethodArgumentNotValidException e) {
    FieldError fe = e.getBindingResult().getFieldError();
    String msg = fe != null ? fe.getDefaultMessage() : ResultCode.BAD_REQUEST.getMessage();
    log.warn("参数校验失败: {}", msg);
    return Result.error(ResultCode.BAD_REQUEST.getCode(), msg);
  }

  @ExceptionHandler(BindException.class)
  public Result<Void> handleBind(BindException e) {
    FieldError fe = e.getBindingResult().getFieldError();
    String msg = fe != null ? fe.getDefaultMessage() : ResultCode.BAD_REQUEST.getMessage();
    return Result.error(ResultCode.BAD_REQUEST.getCode(), msg);
  }

  /** 兜底异常(诊断期把根因拼进 message,便于定位) */
  @ExceptionHandler(Exception.class)
  public Result<Void> handleOther(Exception e, HttpServletResponse resp) {
    log.error("未捕获异常", e);
    Throwable root = e;
    while (root.getCause() != null) {
      root = root.getCause();
    }
    String msg = ResultCode.SERVER_ERROR.getMessage()
        + ": " + root.getClass().getSimpleName() + " - " + root.getMessage();
    return Result.error(ResultCode.SERVER_ERROR.getCode(), msg);
  }
}
