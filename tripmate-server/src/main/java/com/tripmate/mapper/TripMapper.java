package com.tripmate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tripmate.entity.Trip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TripMapper extends BaseMapper<Trip> {

  /** 热门目的地:按所有用户的行程统计目的地出现次数,取前 limit 个 */
  @Select("SELECT destination AS name, COUNT(*) AS cnt FROM trip "
      + "WHERE destination IS NOT NULL AND destination <> '' "
      + "GROUP BY destination ORDER BY cnt DESC LIMIT #{limit}")
  List<Map<String, Object>> selectHotDestinations(@Param("limit") int limit);
}
