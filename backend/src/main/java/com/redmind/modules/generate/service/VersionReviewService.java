package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GeneratedVersion;
import com.redmind.modules.generate.dto.PrePublishCheckItem;
import com.redmind.modules.generate.dto.QualityScores;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class VersionReviewService {

    private final QualityScoreService qualityScoreService;

    public VersionReviewService(QualityScoreService qualityScoreService) {
        this.qualityScoreService = qualityScoreService;
    }

    public GeneratedVersion review(GenerateRequest request, GeneratedVersion version) {
        version.setPrePublishChecks(buildChecks(request, version));
        version.setQualityScores(qualityScoreService.score(request, version));
        version.setPublishSuggestions(buildSuggestions(request, version));
        return version;
    }

    private List<PrePublishCheckItem> buildChecks(GenerateRequest request, GeneratedVersion version) {
        List<PrePublishCheckItem> checks = new ArrayList<>();
        String title = StringUtils.defaultString(version.getTitle());
        String content = StringUtils.defaultString(version.getContent());
        String combined = title + "\n" + content;

        checks.add(PrePublishCheckItem.builder()
            .label("标题长度")
            .status(title.length() > 0 && title.length() <= 20 ? "pass" : "warn")
            .detail(title.length() > 0 && title.length() <= 20 ? "标题长度适合首屏展示" : "建议控制在 20 字以内，更利于停留")
            .build());

        int keywordHits = countKeywordHits(request, combined);
        int keywordTotal = request.getRequiredKeywords() == null ? 0 : request.getRequiredKeywords().size();
        checks.add(PrePublishCheckItem.builder()
            .label("关键词覆盖")
            .status(keywordTotal == 0 || keywordHits > 0 ? "pass" : "warn")
            .detail(keywordTotal == 0 ? "当前没有强制关键词要求" : "已命中 " + keywordHits + "/" + keywordTotal + " 个关键词")
            .build());

        boolean hasForbiddenExpression = containsForbiddenExpression(request, combined);
        checks.add(PrePublishCheckItem.builder()
            .label("禁用表达")
            .status(hasForbiddenExpression ? "warn" : "pass")
            .detail(hasForbiddenExpression ? "命中禁用表达，建议发布前再润色" : "未发现禁用表达")
            .build());

        boolean hasInteraction = StringUtils.containsAny(combined, "评论", "留言", "告诉我", "私信", "想看");
        checks.add(PrePublishCheckItem.builder()
            .label("互动收口")
            .status(hasInteraction ? "pass" : "warn")
            .detail(hasInteraction ? "结尾已包含互动引导" : "建议补一句评论区或私信引导")
            .build());

        boolean hasCollectSignal = StringUtils.containsAny(combined, "收藏", "清单", "步骤", "判断", "对照", "建议");
        checks.add(PrePublishCheckItem.builder()
            .label("收藏价值")
            .status(hasCollectSignal ? "pass" : "warn")
            .detail(hasCollectSignal ? "已有清单/判断信息，适合收藏" : "建议加入清单、步骤或判断标准")
            .build());

        return checks;
    }

    private List<String> buildSuggestions(GenerateRequest request, GeneratedVersion version) {
        List<String> suggestions = new ArrayList<>();
        QualityScores scores = version.getQualityScores();
        if (scores != null && scores.getTitleAttraction() != null && scores.getTitleAttraction() < 4.2D) {
            suggestions.add("标题吸引力偏弱，建议加入反差词、数字或明确结果。");
        }
        if (scores != null && scores.getCollectIntent() != null && scores.getCollectIntent() < 4.0D) {
            suggestions.add("正文可以再补 1 组清单或判断标准，提升收藏意愿。");
        }
        if (scores != null && scores.getInteractionPotential() != null && scores.getInteractionPotential() < 4.0D) {
            suggestions.add("结尾建议补一个二选一问题，让评论更容易发生。");
        }
        if (scores != null && scores.getAiFlavorRisk() != null && scores.getAiFlavorRisk() >= 4.0D) {
            suggestions.add("AI 味偏重，建议减少绝对化表达，多保留第一人称体验。");
        }
        if (containsForbiddenExpression(request, StringUtils.defaultString(version.getTitle()) + version.getContent())) {
            suggestions.add("请先去掉禁用表达，再进行发布。");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("结构、互动和收藏点都比较完整，可以直接进入发布准备。");
        }
        return suggestions;
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

    private boolean containsForbiddenExpression(GenerateRequest request, String combined) {
        if (request.getForbiddenExpressions() == null) {
            return false;
        }
        for (String expression : request.getForbiddenExpressions()) {
            if (StringUtils.isNotBlank(expression) && StringUtils.contains(combined, expression)) {
                return true;
            }
        }
        return false;
    }
}
