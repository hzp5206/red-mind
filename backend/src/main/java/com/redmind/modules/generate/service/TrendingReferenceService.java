package com.redmind.modules.generate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.TrendingReferenceCue;
import com.redmind.modules.trending.dto.TrendingAnalysisResponse;
import com.redmind.modules.trending.entity.TrendingCopyItem;
import com.redmind.modules.trending.mapper.TrendingCopyItemMapper;
import com.redmind.modules.trending.service.TrendingAnalysisEngine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TrendingReferenceService {

    private final TrendingCopyItemMapper trendingCopyItemMapper;
    private final TrendingAnalysisEngine trendingAnalysisEngine;

    public TrendingReferenceService(TrendingCopyItemMapper trendingCopyItemMapper,
                                    TrendingAnalysisEngine trendingAnalysisEngine) {
        this.trendingCopyItemMapper = trendingCopyItemMapper;
        this.trendingAnalysisEngine = trendingAnalysisEngine;
    }

    public void attachReferences(GenerateRequest request) {
        List<String> terms = buildTerms(request);
        List<TrendingCopyItem> recentItems = trendingCopyItemMapper.selectList(new LambdaQueryWrapper<TrendingCopyItem>()
            .orderByDesc(TrendingCopyItem::getHeatScore)
            .orderByDesc(TrendingCopyItem::getFetchedAt)
            .last("limit 30"));
        if (recentItems.isEmpty()) {
            request.setTrendingReferences(Collections.emptyList());
            request.setTrendingStrategyBrief(null);
            return;
        }

        List<TrendingCopyItem> matched = recentItems.stream()
            .filter(item -> matches(item, terms))
            .limit(3)
            .collect(Collectors.toList());
        if (matched.isEmpty()) {
            matched = recentItems.stream().limit(3).collect(Collectors.toList());
        }

        List<TrendingReferenceCue> cues = matched.stream()
            .map(this::toCue)
            .collect(Collectors.toList());
        request.setTrendingReferences(cues);
        request.setTrendingStrategyBrief(buildBrief(cues));
    }

    private TrendingReferenceCue toCue(TrendingCopyItem item) {
        TrendingAnalysisResponse analysis = trendingAnalysisEngine.analyze(item);
        return TrendingReferenceCue.builder()
            .itemId(item.getId())
            .title(item.getTitle())
            .keyword(item.getKeyword())
            .heatScore(item.getHeatScore())
            .hookType(analysis.getHookType())
            .structureSummary(analysis.getStructureSummary())
            .tone(analysis.getTone())
            .keywords(analysis.getRequiredKeywords())
            .collectPoints(analysis.getCollectPoints())
            .build();
    }

    private boolean matches(TrendingCopyItem item, List<String> terms) {
        if (terms.isEmpty()) {
            return true;
        }
        String text = String.join(" ",
            StringUtils.defaultString(item.getKeyword()),
            StringUtils.defaultString(item.getTitle()),
            StringUtils.defaultString(item.getContentText()),
            String.join(" ", trendingAnalysisEngine.parseTags(item.getTagsJson())));
        return terms.stream().anyMatch(term -> StringUtils.isNotBlank(term) && StringUtils.containsIgnoreCase(text, term));
    }

    private List<String> buildTerms(GenerateRequest request) {
        Set<String> terms = new LinkedHashSet<>();
        addTerm(terms, request.getProductName());
        if (request.getRequiredKeywords() != null) {
            request.getRequiredKeywords().forEach(term -> addTerm(terms, term));
        }
        if (request.getCoreSellingPoints() != null) {
            request.getCoreSellingPoints().forEach(term -> addTerm(terms, term));
        }
        splitAndAdd(terms, request.getCoreDescription());
        return new ArrayList<>(terms).stream().limit(8).collect(Collectors.toList());
    }

    private void addTerm(Set<String> terms, String value) {
        String trimmed = StringUtils.trimToEmpty(value);
        if (trimmed.length() >= 2 && trimmed.length() <= 20) {
            terms.add(trimmed);
        }
    }

    private void splitAndAdd(Set<String> terms, String text) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        Arrays.stream(text.split("[,，。；;、\\s]+"))
            .map(StringUtils::trimToEmpty)
            .filter(item -> item.length() >= 2 && item.length() <= 12)
            .limit(6)
            .forEach(terms::add);
    }

    private String buildBrief(List<TrendingReferenceCue> cues) {
        if (cues.isEmpty()) {
            return null;
        }
        List<String> titles = cues.stream().map(TrendingReferenceCue::getTitle).collect(Collectors.toList());
        List<String> hooks = cues.stream().map(TrendingReferenceCue::getHookType).distinct().collect(Collectors.toList());
        return "已参考近期高热样本：" + String.join(" / ", titles) + "。建议优先吸收这些开场方式：" + String.join("、", hooks) + "。";
    }
}
