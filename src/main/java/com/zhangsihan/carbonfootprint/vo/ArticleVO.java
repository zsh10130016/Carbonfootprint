package com.zhangsihan.carbonfootprint.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleVO {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private String author;
    private LocalDateTime publishedAt;
}
