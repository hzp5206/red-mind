package com.redmind.modules.generate.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QualityScores {

    private Double overallScore;
    private Double titleAttraction;
    private Double hookStrength;
    private Double sellingPointClarity;
    private Double emotionalAppeal;
    private Double collectIntent;
    private Double interactionPotential;
    private Double authenticity;
    private Double aiFlavorRisk;
    private String keywordCoverage;
    private String riskLevel;
    private List<String> strengths;
    private List<String> complianceIssues;
}
