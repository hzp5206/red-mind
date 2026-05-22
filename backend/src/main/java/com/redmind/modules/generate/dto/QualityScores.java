package com.redmind.modules.generate.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QualityScores {

    private Double titleAttraction;
    private String keywordDensity;
    private List<String> complianceIssues;
}
