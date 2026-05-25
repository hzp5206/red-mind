package com.redmind.modules.trending.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrendingAnalysisResponse {

    private Long itemId;
    private String titleType;
    private String hookType;
    private String structureSummary;
    private String interactionCta;
    private List<String> collectPoints;
    private List<String> keywords;
    private String tone;
    private String recommendedStyle;
    private String recommendedTone;
    private String recommendedHook;
    private String recommendedStructure;
    private String productHint;
    private String coreDescription;
    private String styleSample;
    private List<String> requiredKeywords;
    private String summary;
    private List<String> adaptationTips;
}
