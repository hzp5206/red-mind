package com.redmind.modules.history.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmind.common.exception.BizException;
import com.redmind.common.security.JwtUserContext;
import com.redmind.modules.generate.dto.QualityScores;
import com.redmind.modules.generate.entity.GenerationHistory;
import com.redmind.modules.generate.entity.Inspiration;
import com.redmind.modules.generate.mapper.GenerationHistoryMapper;
import com.redmind.modules.generate.mapper.InspirationMapper;
import com.redmind.modules.history.dto.HistoryDetailResponse;
import com.redmind.modules.history.dto.HistoryFinalizeRequest;
import com.redmind.modules.history.dto.HistoryPageResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class HistoryService {

    private final GenerationHistoryMapper generationHistoryMapper;
    private final InspirationMapper inspirationMapper;
    private final ObjectMapper objectMapper;

    public HistoryService(GenerationHistoryMapper generationHistoryMapper,
                          InspirationMapper inspirationMapper,
                          ObjectMapper objectMapper) {
        this.generationHistoryMapper = generationHistoryMapper;
        this.inspirationMapper = inspirationMapper;
        this.objectMapper = objectMapper;
    }

    public HistoryPageResponse page(Integer pageNum, Integer pageSize, String style, String startDate, String endDate) {
        Long userId = JwtUserContext.getUserId();
        Page<GenerationHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GenerationHistory> wrapper = new LambdaQueryWrapper<GenerationHistory>()
            .eq(GenerationHistory::getUserId, userId == null ? 0L : userId)
            .orderByDesc(GenerationHistory::getCreatedAt);
        if (StringUtils.isNotBlank(style)) {
            wrapper.eq(GenerationHistory::getStyle, style);
        }
        if (StringUtils.isNotBlank(startDate)) {
            wrapper.ge(GenerationHistory::getCreatedAt, LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.isNotBlank(endDate)) {
            wrapper.le(GenerationHistory::getCreatedAt, LocalDate.parse(endDate).atTime(23, 59, 59));
        }

        Page<GenerationHistory> result = generationHistoryMapper.selectPage(page, wrapper);
        List<HistoryDetailResponse> records = result.getRecords().stream().map(this::toResponse).collect(Collectors.toList());
        return new HistoryPageResponse(result.getTotal(), records);
    }

    public void delete(Long id) {
        Long userId = JwtUserContext.getUserId();
        GenerationHistory history = generationHistoryMapper.selectById(id);
        if (history == null) {
            return;
        }
        if (userId == null || !userId.equals(history.getUserId())) {
            throw new BizException("无权删除该记录");
        }
        generationHistoryMapper.deleteById(id);
    }

    public void collect(Long id) {
        Long userId = JwtUserContext.getUserId();
        if (userId == null) {
            throw new BizException("请先登录后再收藏");
        }
        GenerationHistory history = generationHistoryMapper.selectById(id);
        if (history == null || !userId.equals(history.getUserId())) {
            throw new BizException("仅支持收藏自己的生成记录");
        }

        Long existed = inspirationMapper.selectCount(new LambdaQueryWrapper<Inspiration>()
            .eq(Inspiration::getUserId, userId)
            .eq(Inspiration::getHistoryId, id));
        if (existed != null && existed > 0) {
            return;
        }

        Inspiration inspiration = new Inspiration();
        inspiration.setUserId(userId);
        inspiration.setHistoryId(id);
        inspiration.setCustomTags("自动收藏");
        inspirationMapper.insert(inspiration);

        history.setIsCollected(true);
        generationHistoryMapper.updateById(history);
    }

    public void finalizeVersion(Long id, HistoryFinalizeRequest request) {
        Long userId = JwtUserContext.getUserId();
        if (userId == null) {
            throw new BizException("请先登录后再保存最终版本");
        }
        GenerationHistory history = generationHistoryMapper.selectById(id);
        if (history == null || !userId.equals(history.getUserId())) {
            throw new BizException("仅支持保存自己的生成记录");
        }

        try {
            history.setFinalTitle(request.getVersion().getTitle());
            history.setFinalResult(objectMapper.writeValueAsString(request.getVersion()));
            history.setOptimizationActions(objectMapper.writeValueAsString(request.getVersion().getOptimizationActions()));
            QualityScores qualityScores = request.getVersion().getQualityScores();
            history.setFinalScore(qualityScores == null ? null : qualityScores.getOverallScore());
            history.setLastModifiedAt(LocalDateTime.now());
            generationHistoryMapper.updateById(history);
        } catch (Exception exception) {
            throw new BizException("保存最终采用版本失败");
        }
    }

    private HistoryDetailResponse toResponse(GenerationHistory history) {
        HistoryDetailResponse response = new HistoryDetailResponse();
        response.setId(history.getId());
        response.setCoreInput(history.getCoreInput());
        response.setStyle(history.getStyle());
        response.setPersona(history.getPersona());
        response.setWordCount(history.getWordCount());
        response.setResults(history.getResults());
        response.setFinalTitle(history.getFinalTitle());
        response.setFinalResult(history.getFinalResult());
        response.setFinalScore(history.getFinalScore());
        response.setIsCollected(history.getIsCollected());
        response.setLastModifiedAt(history.getLastModifiedAt() == null ? null
            : history.getLastModifiedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.setCreatedAt(history.getCreatedAt() == null ? null
            : history.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return response;
    }
}
