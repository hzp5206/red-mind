package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GeneratedVersion;
import com.redmind.modules.generate.dto.PrePublishCheckItem;
import com.redmind.modules.generate.dto.QualityScores;
import com.redmind.modules.moderation.service.SensitiveWordService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class QualityScoreService {

    private final SensitiveWordService sensitiveWordService;

    public QualityScoreService(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    public QualityScores score(GenerateRequest request, GeneratedVersion version) {
        String title = StringUtils.defaultString(version.getTitle());
        String content = StringUtils.defaultString(version.getContent());
        String combined = title + "\n" + content;

        double titleScore = clamp(scoreTitle(title), 1.0D, 5.0D);
        double hookStrength = clamp(scoreHook(version, combined), 1.0D, 5.0D);
        double sellingPointClarity = clamp(scoreSellingPoints(request, combined), 1.0D, 5.0D);
        double emotionalAppeal = clamp(scoreEmotionalAppeal(combined), 1.0D, 5.0D);
        double collectIntent = clamp(scoreCollectIntent(combined), 1.0D, 5.0D);
        double interactionPotential = clamp(scoreInteractionPotential(version, combined), 1.0D, 5.0D);
        double authenticity = clamp(scoreAuthenticity(version, combined), 1.0D, 5.0D);
        double aiFlavorRisk = clamp(scoreAiFlavorRisk(version, combined), 1.0D, 5.0D);

        int keywordHits = countKeywordHits(request, combined);
        String keywordCoverage = keywordHits >= 3 ? "rich" : keywordHits >= 1 ? "good" : "basic";

        List<String> issues = new ArrayList<>();
        List<String> sensitiveWords = sensitiveWordService.detect(combined);
        if (sensitiveWords.isEmpty()) {
            issues.add("未发现敏感词");
        } else {
            for (String word : sensitiveWords) {
                issues.add("检测到敏感表达：" + word);
            }
        }
        if (request.getForbiddenExpressions() != null) {
            for (String expression : request.getForbiddenExpressions()) {
                if (StringUtils.isNotBlank(expression) && StringUtils.contains(combined, expression)) {
                    issues.add("命中禁用表达：" + expression);
                }
            }
        }

        String riskLevel = issues.size() > 1 ? "medium" : "low";
        if (issues.stream().anyMatch(item -> item.contains("命中禁用表达"))) {
            riskLevel = "high";
        }

        List<String> strengths = buildStrengths(
            titleScore,
            hookStrength,
            sellingPointClarity,
            emotionalAppeal,
            collectIntent,
            interactionPotential,
            authenticity
        );
        mergeChecklistStrengths(strengths, version.getPrePublishChecks());

        double overallScore = clamp(
            (titleScore + hookStrength + sellingPointClarity + emotionalAppeal
                + collectIntent + interactionPotential + authenticity) / 7.0D
                - ((aiFlavorRisk - 3.0D) * 0.15D),
            1.0D,
            5.0D
        );

        return QualityScores.builder()
            .overallScore(overallScore)
            .titleAttraction(titleScore)
            .hookStrength(hookStrength)
            .sellingPointClarity(sellingPointClarity)
            .emotionalAppeal(emotionalAppeal)
            .collectIntent(collectIntent)
            .interactionPotential(interactionPotential)
            .authenticity(authenticity)
            .aiFlavorRisk(aiFlavorRisk)
            .keywordCoverage(keywordCoverage)
            .riskLevel(riskLevel)
            .strengths(strengths)
            .complianceIssues(issues)
            .build();
    }

    private double scoreTitle(String title) {
        double score = 3.2D;
        if (StringUtils.containsAny(title, "？", "?", "别", "先", "真心", "后悔")) {
            score += 0.7D;
        }
        if (title.matches(".*[0-9一二三四五六七八九十].*")) {
            score += 0.5D;
        }
        if (title.length() >= 10 && title.length() <= 20) {
            score += 0.4D;
        }
        if (StringUtils.containsAny(title, "清单", "避坑", "真实", "值得")) {
            score += 0.3D;
        }
        return score;
    }

    private double scoreHook(GeneratedVersion version, String combined) {
        double score = 3.0D;
        if (StringUtils.containsAny(StringUtils.defaultString(version.getOpening()), "先别", "先说结论", "如果你也", "真心话")) {
            score += 1.0D;
        }
        if (StringUtils.containsAny(StringUtils.defaultString(version.getHookType()), "反差", "体验", "清单")) {
            score += 0.6D;
        }
        if (StringUtils.containsAny(combined, "踩坑", "讲透", "真实感受", "直接给你")) {
            score += 0.4D;
        }
        return score;
    }

    private double scoreSellingPoints(GenerateRequest request, String combined) {
        if (request.getCoreSellingPoints() == null || request.getCoreSellingPoints().isEmpty()) {
            return 3.8D;
        }
        int hits = 0;
        for (String point : request.getCoreSellingPoints()) {
            if (StringUtils.isNotBlank(point) && StringUtils.contains(combined, point)) {
                hits++;
            }
        }
        double ratio = (double) hits / (double) request.getCoreSellingPoints().size();
        return 2.8D + (ratio * 2.0D);
    }

    private double scoreEmotionalAppeal(String combined) {
        int hits = countMatches(combined, Arrays.asList("真的", "真心", "安心", "惊喜", "后悔", "犹豫", "纠结"));
        return 3.0D + Math.min(1.6D, hits * 0.25D);
    }

    private double scoreCollectIntent(String combined) {
        int hits = countMatches(combined, Arrays.asList("收藏", "清单", "下次", "判断", "步骤", "建议", "对照"));
        return 3.0D + Math.min(1.7D, hits * 0.28D);
    }

    private double scoreInteractionPotential(GeneratedVersion version, String combined) {
        int hits = countMatches(combined, Arrays.asList("评论区", "留言", "告诉我", "你更", "想看", "私信"));
        if (StringUtils.isNotBlank(version.getCta())) {
            hits++;
        }
        return 3.0D + Math.min(1.5D, hits * 0.3D);
    }

    private double scoreAuthenticity(GeneratedVersion version, String combined) {
        int hits = countMatches(combined, Arrays.asList("我", "自己", "用了", "感受", "体验", "场景", "犹豫"));
        if (StringUtils.isNotBlank(version.getStrategySummary()) && StringUtils.contains(version.getStrategySummary(), "真实")) {
            hits++;
        }
        return 3.0D + Math.min(1.5D, hits * 0.18D);
    }

    private double scoreAiFlavorRisk(GeneratedVersion version, String combined) {
        double risk = 2.2D;
        if (StringUtils.countMatches(combined, "非常") >= 3 || StringUtils.countMatches(combined, "真的") >= 4) {
            risk += 0.8D;
        }
        if (StringUtils.isNotBlank(version.getStrategySummary()) && version.getStrategySummary().length() > 90) {
            risk += 0.4D;
        }
        if (StringUtils.countMatches(combined, "✨") >= 3 || StringUtils.countMatches(combined, "🔥") >= 3) {
            risk += 0.6D;
        }
        return risk;
    }

    private int countKeywordHits(GenerateRequest request, String combined) {
        int hitCount = 0;
        if (request.getRequiredKeywords() == null) {
            return hitCount;
        }
        for (String keyword : request.getRequiredKeywords()) {
            if (StringUtils.isNotBlank(keyword) && StringUtils.contains(combined, keyword)) {
                hitCount++;
            }
        }
        return hitCount;
    }

    private int countMatches(String text, List<String> patterns) {
        int count = 0;
        for (String pattern : patterns) {
            if (StringUtils.contains(text, pattern)) {
                count++;
            }
        }
        return count;
    }

    private List<String> buildStrengths(double titleScore,
                                        double hookStrength,
                                        double sellingPointClarity,
                                        double emotionalAppeal,
                                        double collectIntent,
                                        double interactionPotential,
                                        double authenticity) {
        List<String> strengths = new ArrayList<>();
        if (titleScore >= 4.4D) {
            strengths.add("标题有停留点");
        }
        if (hookStrength >= 4.3D) {
            strengths.add("开头钩子比较强");
        }
        if (sellingPointClarity >= 4.2D) {
            strengths.add("卖点表达清晰");
        }
        if (collectIntent >= 4.2D) {
            strengths.add("具备较强收藏价值");
        }
        if (interactionPotential >= 4.2D) {
            strengths.add("互动引导自然");
        }
        if (authenticity >= 4.2D || emotionalAppeal >= 4.2D) {
            strengths.add("真人感和情绪感都不错");
        }
        if (strengths.isEmpty()) {
            strengths.add("结构完整，可继续细化标题和开头");
        }
        return strengths;
    }

    private void mergeChecklistStrengths(List<String> strengths, List<PrePublishCheckItem> checks) {
        if (checks == null) {
            return;
        }
        for (PrePublishCheckItem check : checks) {
            if (check != null && "pass".equalsIgnoreCase(check.getStatus())) {
                String message = check.getLabel() + "已通过";
                if (!strengths.contains(message)) {
                    strengths.add(message);
                }
            }
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
