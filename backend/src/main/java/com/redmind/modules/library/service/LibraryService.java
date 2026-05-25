package com.redmind.modules.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmind.common.exception.BizException;
import com.redmind.common.security.JwtUserContext;
import com.redmind.modules.generate.entity.GenerationHistory;
import com.redmind.modules.generate.entity.Inspiration;
import com.redmind.modules.generate.mapper.GenerationHistoryMapper;
import com.redmind.modules.generate.mapper.InspirationMapper;
import com.redmind.modules.library.dto.LibraryItemResponse;
import com.redmind.modules.trending.dto.TrendingAnalysisResponse;
import com.redmind.modules.trending.entity.TrendingCopyItem;
import com.redmind.modules.trending.mapper.TrendingCopyItemMapper;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LibraryService {

    private final InspirationMapper inspirationMapper;
    private final GenerationHistoryMapper generationHistoryMapper;
    private final TrendingCopyItemMapper trendingCopyItemMapper;
    private final ObjectMapper objectMapper;

    public LibraryService(InspirationMapper inspirationMapper,
                          GenerationHistoryMapper generationHistoryMapper,
                          TrendingCopyItemMapper trendingCopyItemMapper,
                          ObjectMapper objectMapper) {
        this.inspirationMapper = inspirationMapper;
        this.generationHistoryMapper = generationHistoryMapper;
        this.trendingCopyItemMapper = trendingCopyItemMapper;
        this.objectMapper = objectMapper;
    }

    public List<LibraryItemResponse> myCollections() {
        Long userId = JwtUserContext.getUserId();
        if (userId == null) {
            return Collections.emptyList();
        }

        List<Inspiration> inspirations = inspirationMapper.selectList(new LambdaQueryWrapper<Inspiration>()
            .eq(Inspiration::getUserId, userId)
            .orderByDesc(Inspiration::getCreatedAt)
            .orderByDesc(Inspiration::getId));

        Set<Long> historyIds = inspirations.stream()
            .map(Inspiration::getHistoryId)
            .filter(item -> item != null)
            .collect(Collectors.toSet());
        Set<Long> trendingItemIds = inspirations.stream()
            .map(Inspiration::getTrendingItemId)
            .filter(item -> item != null)
            .collect(Collectors.toSet());

        Map<Long, GenerationHistory> historyMap = historyIds.isEmpty() ? Collections.emptyMap()
            : generationHistoryMapper.selectBatchIds(historyIds).stream()
                .collect(Collectors.toMap(GenerationHistory::getId, Function.identity()));
        Map<Long, TrendingCopyItem> trendingMap = trendingItemIds.isEmpty() ? Collections.emptyMap()
            : trendingCopyItemMapper.selectBatchIds(trendingItemIds).stream()
                .collect(Collectors.toMap(TrendingCopyItem::getId, Function.identity()));

        return inspirations.stream()
            .map(item -> toResponse(item, historyMap.get(item.getHistoryId()), trendingMap.get(item.getTrendingItemId())))
            .collect(Collectors.toList());
    }

    public void deleteCollection(Long id) {
        Long userId = JwtUserContext.getUserId();
        Inspiration inspiration = inspirationMapper.selectById(id);
        if (inspiration == null) {
            return;
        }
        if (userId == null || !userId.equals(inspiration.getUserId())) {
            throw new BizException("无权删除该收藏");
        }
        inspirationMapper.deleteById(id);
    }

    private LibraryItemResponse toResponse(Inspiration inspiration, GenerationHistory history, TrendingCopyItem trendingItem) {
        LibraryItemResponse response = new LibraryItemResponse();
        response.setId(inspiration.getId());
        response.setHistoryId(inspiration.getHistoryId());
        response.setTrendingItemId(inspiration.getTrendingItemId());
        response.setCustomTags(inspiration.getCustomTags());
        if (history != null) {
            response.setSourceType("history");
            response.setSourceTitle(history.getFinalTitle());
            response.setProductName(history.getFinalTitle());
            response.setCoreInput(history.getCoreInput());
            response.setPreviewText(history.getFinalResult() == null ? history.getResults() : history.getFinalResult());
            response.setStyle(history.getStyle());
            response.setResults(history.getResults());
        }
        if (trendingItem != null) {
            TrendingAnalysisResponse analysis = parseAnalysis(trendingItem.getAnalysisJson());
            response.setSourceType("trending");
            response.setSourceTitle(trendingItem.getTitle());
            response.setProductName(analysis == null ? trendingItem.getKeyword() : analysis.getProductHint());
            response.setCoreInput(analysis == null ? trendingItem.getContentText() : analysis.getCoreDescription());
            response.setPreviewText(trendingItem.getContentText());
            response.setStyle(analysis == null ? "good_item" : analysis.getRecommendedStyle());
            response.setTone(analysis == null ? null : analysis.getRecommendedTone());
            response.setResults(trendingItem.getContentText());
            response.setNoteUrl(trendingItem.getNoteUrl());
            response.setStyleSample(analysis == null ? trendingItem.getContentText() : analysis.getStyleSample());
            response.setRequiredKeywords(analysis == null ? Collections.emptyList() : analysis.getRequiredKeywords());
            response.setHookPreference(analysis == null ? null : analysis.getRecommendedHook());
            response.setNoteStructure(analysis == null ? null : analysis.getRecommendedStructure());
        }
        response.setCreatedAt(inspiration.getCreatedAt() == null ? null :
            inspiration.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return response;
    }

    private TrendingAnalysisResponse parseAnalysis(String analysisJson) {
        if (StringUtils.isBlank(analysisJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(analysisJson, TrendingAnalysisResponse.class);
        } catch (Exception exception) {
            return null;
        }
    }
}
