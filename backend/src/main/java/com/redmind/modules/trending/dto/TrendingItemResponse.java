package com.redmind.modules.trending.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrendingItemResponse {

    private Long id;
    private Long taskId;
    private String platformCode;
    private String sourceId;
    private String keyword;
    private String title;
    private String contentText;
    private String authorName;
    private String noteUrl;
    private Integer likesCount;
    private Integer collectsCount;
    private Integer commentsCount;
    private Integer heatScore;
    private List<String> tags;
    private String publishedAt;
    private String fetchedAt;
}
