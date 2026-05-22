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
    private Boolean isCollected;
    private String createdAt;
}
