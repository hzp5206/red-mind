package com.redmind.modules.trending.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrendingDashboardResponse {

    private Long taskCount;
    private Long enabledTaskCount;
    private Long totalItemCount;
    private Long todayFetchedCount;
    private List<TrendingTaskResponse> tasks;
    private List<TrendingItemResponse> latestItems;
}
