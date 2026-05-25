package com.redmind.modules.generate.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedVersion {

    private Integer verNum;
    private String angleLabel;
    private String hookType;
    private String strategySummary;
    private String opening;
    private String cta;
    private String title;
    private List<TitleCandidate> titleCandidates;
    private String content;
    private List<String> tags;
    private List<String> publishSuggestions;
    private List<PrePublishCheckItem> prePublishChecks;
    private List<String> optimizationActions;
    private QualityScores qualityScores;
}
