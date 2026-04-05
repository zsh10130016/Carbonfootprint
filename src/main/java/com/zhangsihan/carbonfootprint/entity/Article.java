package com.zhangsihan.carbonfootprint.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Article {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private String author;
    private LocalDateTime publishedAt;
}
