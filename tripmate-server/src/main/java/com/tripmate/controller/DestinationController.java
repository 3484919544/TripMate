package com.tripmate.controller;

import com.tripmate.common.Result;
import com.tripmate.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 目的地统计接口
 */
@RestController
@RequestMapping("/api/destination")
@RequiredArgsConstructor
public class DestinationController {

  private final TripService tripService;

  /** 热门目的地:根据所有用户的行程动态统计,返回热度最高的前 6 个 */
  @GetMapping("/hot")
  public Result<List<Map<String, Object>>> hot() {
    return Result.ok(tripService.hotDestinations(6));
  }
}
