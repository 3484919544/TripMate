package com.tripmate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tripmate.ai.AiClient;
import com.tripmate.ai.ContentSafetyFilter;
import com.tripmate.ai.PromptTemplate;
import com.tripmate.common.BusinessException;
import com.tripmate.common.PageResult;
import com.tripmate.common.ResultCode;
import com.tripmate.dto.request.GenerateTripRequest;
import com.tripmate.entity.Trip;
import com.tripmate.entity.TripDay;
import com.tripmate.mapper.TripDayMapper;
import com.tripmate.mapper.TripMapper;
import com.tripmate.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 行程业务:生成 / 保存 / 列表 / 详情 / 修改 / 删除
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {

  private final TripMapper tripMapper;
  private final TripDayMapper tripDayMapper;
  private final AiClient aiClient;
  private final PromptTemplate promptTemplate;
  private final ContentSafetyFilter safetyFilter;

  /** 热门目的地:根据所有用户的行程统计,返回热度最高的前 limit 个 [{name, cnt}] */
  public List<Map<String, Object>> hotDestinations(int limit) {
    return tripMapper.selectHotDestinations(limit);
  }

  /** 调大模型生成行程(不落库) */
  public Trip generate(GenerateTripRequest req) {
    Long userId = UserContext.getUserId();
    // 输入安全过滤
    String origin = safetyFilter.sanitizeInput(req.getOrigin());
    String destination = safetyFilter.sanitizeInput(req.getDestination());
    String preference = safetyFilter.sanitizeInput(req.getPreference());
    String notes = safetyFilter.sanitizeInput(req.getNotes());

    GenerateTripRequest safe = new GenerateTripRequest();
    safe.setOrigin(origin);
    safe.setDestination(destination);
    safe.setDays(req.getDays());
    safe.setBudget(req.getBudget());
    safe.setPreference(preference);
    safe.setNotes(notes);

    String userPrompt = promptTemplate.buildUserPrompt(safe);
    String logPrompt = promptTemplate.buildLogPrompt(safe);

    return aiClient.generateTrip(
        PromptTemplate.SYSTEM_PROMPT, userPrompt, logPrompt, userId, req.getBudget());
  }

  /** 保存行程(标题/天数/每日安排落库) */
  @Transactional(rollbackFor = Exception.class)
  public Long save(Trip trip) {
    Long userId = UserContext.getUserId();
    trip.setId(null);
    trip.setUserId(userId);
    if (trip.getStatus() == null) {
      trip.setStatus("SAVED");
    }
    tripMapper.insert(trip);
    saveDays(trip.getId(), trip.getDayList());
    return trip.getId();
  }

  /** 行程列表(当前用户,支持目的地/标题关键词与最大天数筛选) */
  public PageResult<Trip> list(Integer pageNum, Integer pageSize, String keyword, Integer maxDays) {
    Long userId = UserContext.getUserId();
    int num = pageNum == null || pageNum < 1 ? 1 : pageNum;
    int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);

    LambdaQueryWrapper<Trip> w = new LambdaQueryWrapper<>();
    w.eq(Trip::getUserId, userId);
    if (keyword != null && !keyword.isBlank()) {
      String kw = keyword.trim();
      w.and(q -> q.like(Trip::getDestination, kw).or().like(Trip::getTitle, kw));
    }
    if (maxDays != null) {
      w.le(Trip::getDays, maxDays);
    }
    w.orderByDesc(Trip::getCreateTime);

    Page<Trip> page = tripMapper.selectPage(new Page<>(num, size), w);
    return new PageResult<>(page.getRecords(), page.getTotal());
  }

  /** 行程详情(含每日安排) */
  public Trip getById(Long id) {
    Trip trip = getOwned(id);
    List<TripDay> days = tripDayMapper.selectList(
        new LambdaQueryWrapper<TripDay>()
            .eq(TripDay::getTripId, id)
            .orderByAsc(TripDay::getDayIndex));
    trip.setDayList(days);
    return trip;
  }

  /** 修改行程(整体替换每日安排) */
  @Transactional(rollbackFor = Exception.class)
  public void update(Long id, Trip trip) {
    getOwned(id);
    Long userId = UserContext.getUserId();
    trip.setId(id);
    trip.setUserId(userId);
    tripMapper.updateById(trip);
    tripDayMapper.delete(new LambdaQueryWrapper<TripDay>().eq(TripDay::getTripId, id));
    saveDays(id, trip.getDayList());
  }

  /** 删除行程(级联删每日安排) */
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id) {
    getOwned(id);
    tripDayMapper.delete(new LambdaQueryWrapper<TripDay>().eq(TripDay::getTripId, id));
    tripMapper.deleteById(id);
  }

  // ---------- 私有 ----------

  private void saveDays(Long tripId, List<TripDay> days) {
    if (days == null || days.isEmpty()) {
      return;
    }
    for (TripDay d : days) {
      d.setId(null);
      d.setTripId(tripId);
      tripDayMapper.insert(d);
    }
  }

  /** 取当前用户拥有的行程,否则 404/403 */
  private Trip getOwned(Long id) {
    Long userId = UserContext.getUserId();
    Trip t = tripMapper.selectById(id);
    if (t == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "行程不存在");
    }
    if (!t.getUserId().equals(userId)) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该行程");
    }
    return t;
  }
}
