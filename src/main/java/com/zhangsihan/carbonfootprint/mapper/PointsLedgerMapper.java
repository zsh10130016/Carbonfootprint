package com.zhangsihan.carbonfootprint.mapper;

import com.zhangsihan.carbonfootprint.entity.PointsLedger;
import com.zhangsihan.carbonfootprint.vo.RankingItemVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PointsLedgerMapper {
    int insert(PointsLedger ledger);

    int deleteBySourceRecordIdAndUserId(@Param("sourceRecordId") Long sourceRecordId, @Param("userId") Long userId);

    Integer sumPointsByUserId(Long userId);

    List<RankingItemVO> findAllRankings();
}
