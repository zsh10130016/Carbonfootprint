package com.zhangsihan.carbonfootprint.controller;

import com.zhangsihan.carbonfootprint.common.ApiResponse;
import com.zhangsihan.carbonfootprint.service.ArticleService;
import com.zhangsihan.carbonfootprint.vo.ArticleVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<List<ArticleVO>> list() {
        return ApiResponse.success(articleService.listArticles());
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleVO> detail(@PathVariable Long id) {
        return ApiResponse.success(articleService.getArticle(id));
    }
}
