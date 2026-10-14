package com.tripmate.controller;

import com.tripmate.common.PageResult;
import com.tripmate.common.Result;
import com.tripmate.dto.request.GenerateTripRequest;
import com.tripmate.entity.Trip;
import com.tripmate.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 行程接口:生成 / 保存 / 列表 / 详情 / 修改 / 删除
 */
@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
public class TripController {

  private final TripService tripService;

  /** 生成行程(调 AI,不落库) */
  @PostMapping("/generate")
  public Result<Trip> generate(@Valid @RequestBody GenerateTripRequest req) {
    return Result.ok(tripService.generate(req));
  }

  /** 保存行程 */
  @PostMapping("/save")
  public Result<Map<String, Object>> save(@RequestBody Trip trip) {
    Long id = tripService.save(trip);
    return Result.ok(Map.of("id", id));
  }

  /** 行程列表 */
  @GetMapping("/list")
  public Result<PageResult<Trip>> list(@RequestParam(required = false) Integer pageNum,
                                        @RequestParam(required = false) Integer pageSize,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Integer maxDays) {
    return Result.ok(tripService.list(pageNum, pageSize, keyword, maxDays));
  }

  /** 行程详情 */
  @GetMapping("/{id}")
  public Result<Trip> getById(@PathVariable Long id) {
    return Result.ok(tripService.getById(id));
  }

  /** 修改行程 */
  @PutMapping("/{id}")
  public Result<Void> update(@PathVariable Long id, @RequestBody Trip trip) {
    tripService.update(id, trip);
    return Result.ok();
  }

  /** 删除行程 */
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    tripService.delete(id);
    return Result.ok();
  }
}
