package com.redmind.modules.generate.dto;

import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateRequest {

    @NotBlank(message = "创作模式不能为空")
    private String mode;

    private String productName;

    @NotBlank(message = "核心描述不能为空")
    private String coreDescription;

    @NotBlank(message = "笔记风格不能为空")
    private String style;

    private List<String> targetAudience;
    private List<String> coreSellingPoints;
    private List<String> useScenarios;
    private String tone;
    private String conversionGoal;
    private String contentGoal;
    private String hookPreference;
    private String noteStructure;

    @NotNull(message = "字数不能为空")
    @Min(value = 100, message = "字数不能少于100")
    @Max(value = 1000, message = "字数不能超过1000")
    private Integer wordCount;

    private List<String> requiredKeywords;
    private List<String> forbiddenExpressions;
    private String referenceUrl;
    private String styleSample;
    private List<TrendingReferenceCue> trendingReferences;
    private String trendingStrategyBrief;
}
