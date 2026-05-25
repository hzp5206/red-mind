package com.redmind.modules.generate.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrendingReferenceCue {

    private Long itemId;
    private String title;
    private String keyword;
    private Integer heatScore;
    private String hookType;
    private String structureSummary;
    private String tone;
    private List<String> keywords;
    private List<String> collectPoints;
}
