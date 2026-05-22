package com.redmind.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.redmind.modules.common.dto.PageResponse;
import com.redmind.modules.generate.entity.GenerationHistory;
import com.redmind.modules.generate.mapper.GenerationHistoryMapper;
import com.redmind.modules.log.dto.GenerationLogQueryRequest;
import com.redmind.modules.log.dto.GenerationLogResponse;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GenerationLogService {

    private final GenerationHistoryMapper generationHistoryMapper;

    public GenerationLogService(GenerationHistoryMapper generationHistoryMapper) {
        this.generationHistoryMapper = generationHistoryMapper;
    }

    public PageResponse<GenerationLogResponse> page(GenerationLogQueryRequest request, Integer pageNum, Integer pageSize) {
        Page<GenerationHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GenerationHistory> wrapper = new LambdaQueryWrapper<GenerationHistory>().orderByDesc(GenerationHistory::getId);
        if (request != null) {
            if (request.getUserId() != null) {
                wrapper.eq(GenerationHistory::getUserId, request.getUserId());
            }
            if (StringUtils.isNotBlank(request.getStyle())) {
                wrapper.eq(GenerationHistory::getStyle, request.getStyle());
            }
            if (request.getStartDate() != null) {
                wrapper.ge(GenerationHistory::getCreatedAt, request.getStartDate().atStartOfDay());
            }
            if (request.getEndDate() != null) {
                wrapper.le(GenerationHistory::getCreatedAt, LocalDateTime.of(request.getEndDate(), LocalTime.MAX));
            }
        }
        Page<GenerationHistory> result = generationHistoryMapper.selectPage(page, wrapper);
        return new PageResponse<>(
            result.getTotal(),
            result.getRecords().stream().map(item -> GenerationLogResponse.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .coreInput(item.getCoreInput())
                .style(item.getStyle())
                .persona(item.getPersona())
                .wordCount(item.getWordCount())
                .collected(item.getIsCollected())
                .createdAt(item.getCreatedAt() == null ? null : item.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build())
                .collect(Collectors.toList())
        );
    }
}
