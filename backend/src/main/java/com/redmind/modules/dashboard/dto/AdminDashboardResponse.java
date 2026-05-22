package com.redmind.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardResponse {

    private Long templateCount;
    private Long userCount;
    private Long adminCount;
    private Long todayGenerationCount;
    private Long sensitiveWordCount;
    private Long todayOperationCount;
}
