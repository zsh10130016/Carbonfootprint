package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.mapper.CarbonRecordMapper;
import com.zhangsihan.carbonfootprint.mapper.PointsLedgerMapper;
import com.zhangsihan.carbonfootprint.security.SecurityUtils;
import com.zhangsihan.carbonfootprint.vo.CommunityProfileVO;
import com.zhangsihan.carbonfootprint.vo.RankingItemVO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PointsLedgerMapper pointsLedgerMapper;
    private final CarbonRecordMapper carbonRecordMapper;

    public List<RankingItemVO> getRankings() {
        List<RankingItemVO> raw = pointsLedgerMapper.findAllRankings();
        List<RankingItemVO> rankings = new ArrayList<>();
        int rank = 1;
        for (RankingItemVO item : raw) {
            rankings.add(RankingItemVO.builder()
                    .rank(rank)
                    .userId(item.getUserId())
                    .username(item.getUsername())
                    .fullName(item.getFullName())
                    .totalPoints(item.getTotalPoints())
                    .badge(resolveBadge(rank))
                    .build());
            rank++;
        }
        return rankings;
    }

    public CommunityProfileVO getMyCommunityProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<RankingItemVO> rankings = getRankings();
        Integer rank = rankings.stream()
                .filter(item -> item.getUserId().equals(userId))
                .map(RankingItemVO::getRank)
                .findFirst()
                .orElse(rankings.size() + 1);
        Integer totalPoints = pointsLedgerMapper.sumPointsByUserId(userId);
        Integer totalRecords = carbonRecordMapper.countByUserId(userId);
        BigDecimal totalEmission = carbonRecordMapper.sumEmissionByUserId(userId);
        return CommunityProfileVO.builder()
                .rank(rank)
                .totalPoints(totalPoints == null ? 0 : totalPoints)
                .totalRecords(totalRecords == null ? 0 : totalRecords)
                .totalEmission(totalEmission == null ? BigDecimal.ZERO : totalEmission)
                .build();
    }

    private String resolveBadge(int rank) {
        return switch (rank) {
            case 1 -> "低碳领航者";
            case 2 -> "绿色进阶者";
            case 3 -> "节能闪光者";
            default -> "环保行动派";
        };
    }
}
