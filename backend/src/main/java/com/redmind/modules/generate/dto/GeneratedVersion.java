package com.redmind.modules.generate.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeneratedVersion {

    private Integer verNum;
    private String title;
    private String content;
    private List<String> tags;
    private QualityScores qualityScores;
}
