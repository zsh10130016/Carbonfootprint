package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import com.zhangsihan.carbonfootprint.entity.Article;
import com.zhangsihan.carbonfootprint.mapper.ArticleMapper;
import com.zhangsihan.carbonfootprint.vo.ArticleVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;

    public List<ArticleVO> listArticles() {
        return articleMapper.findAll().stream().map(this::toVO).toList();
    }

    public ArticleVO getArticle(Long id) {
        Article article = articleMapper.findById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资讯不存在");
        }
        return toVO(article);
    }

    private ArticleVO toVO(Article article) {
        return ArticleVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .coverImage(article.getCoverImage())
                .author(article.getAuthor())
                .publishedAt(article.getPublishedAt())
                .build();
    }
}
