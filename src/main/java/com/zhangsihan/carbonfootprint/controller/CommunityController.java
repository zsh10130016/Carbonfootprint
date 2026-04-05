package com.zhangsihan.carbonfootprint.controller;

import com.zhangsihan.carbonfootprint.common.ApiResponse;
import com.zhangsihan.carbonfootprint.service.CommunityService;
import com.zhangsihan.carbonfootprint.vo.CommunityProfileVO;
import com.zhangsihan.carbonfootprint.vo.RankingItemVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/rankings")
    public ApiResponse<List<RankingItemVO>> rankings() {
        return ApiResponse.success(communityService.getRankings());
    }

    @GetMapping("/me")
    public ApiResponse<CommunityProfileVO> me() {
        return ApiResponse.success(communityService.getMyCommunityProfile());
    }
}
