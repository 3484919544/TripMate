package com.tripmate.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果(前端读取 data.list)
 */
@Data
public class PageResult<T> {
  private List<T> list;
  private long total;

  public PageResult(List<T> list, long total) {
    this.list = list;
    this.total = total;
  }
}
