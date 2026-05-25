package com.redmind.modules.history.dto;

import lombok.Data;

@Data
public class HistoryDetailResponse {

    private Long id;
    private String coreInput;
    private String style;
    private String persona;
    private Integer wordCount;
    private String results;
    private String finalTitle;
    private String finalResult;
    private Double finalScore;
    private Boolean isCollected;
    private String lastModifiedAt;
    private String createdAt;
}
