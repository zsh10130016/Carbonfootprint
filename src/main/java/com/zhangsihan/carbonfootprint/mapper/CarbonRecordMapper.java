package com.zhangsihan.carbonfootprint.mapper;

import com.zhangsihan.carbonfootprint.dto.CarbonRecordQueryRequest;
import com.zhangsihan.carbonfootprint.entity.CarbonRecord;
import com.zhangsihan.carbonfootprint.vo.CategoryStatVO;
import com.zhangsihan.carbonfootprint.vo.SourceRatioVO;
import com.zhangsihan.carbonfootprint.vo.TrendPointVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CarbonRecordMapper {
    int insert(CarbonRecord record);

    int updateByIdAndUserId(CarbonRecord record);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    CarbonRecord findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<CarbonRecord> findByCondition(@Param("userId") Long userId, @Param("query") CarbonRecordQueryRequest query);

    BigDecimal sumEmissionByUserId(Long userId);

    BigDecimal sumEmissionSince(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);

    BigDecimal sumEmissionByActivitySince(@Param("userId") Long userId,
                                          @Param("activityType") String activityType,
                                          @Param("startTime") LocalDateTime startTime);

    Integer countByUserId(Long userId);

    String findTopSource(Long userId);

    List<TrendPointVO> findTrend(@Param("userId") Long userId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    List<SourceRatioVO> findSourceRatios(Long userId);

    List<CategoryStatVO> findCategoryStats(Long userId);
}
