package com.redmind.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SummaryBucketItem {

    private String label;
    private Long value;
}
