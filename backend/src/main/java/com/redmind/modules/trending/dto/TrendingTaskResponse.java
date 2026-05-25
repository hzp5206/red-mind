package com.redmind.modules.trending.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrendingTaskResponse {

    private Long id;
    private String taskName;
    private String platformCode;
    private String keywords;
    private Integer fetchLimit;
    private String cronExpr;
    private String providerCode;
    private Boolean enabled;
    private String lastRunAt;
    private Integer lastFetchedCount;
    private String lastStatus;
    private String lastMessage;
    private String createdAt;
    private String updatedAt;
}
