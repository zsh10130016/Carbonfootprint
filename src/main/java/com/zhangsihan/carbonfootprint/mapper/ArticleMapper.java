package com.zhangsihan.carbonfootprint.mapper;

import com.zhangsihan.carbonfootprint.entity.Article;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper {
    List<Article> findAll();

    Article findById(Long id);
}
