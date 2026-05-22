package com.redmind.modules.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redmind.common.exception.BizException;
import com.redmind.common.security.JwtUserContext;
import com.redmind.modules.generate.entity.GenerationHistory;
import com.redmind.modules.generate.entity.Inspiration;
import com.redmind.modules.generate.mapper.GenerationHistoryMapper;
import com.redmind.modules.generate.mapper.InspirationMapper;
import com.redmind.modules.library.dto.LibraryItemResponse;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LibraryService {

    private final InspirationMapper inspirationMapper;
    private final GenerationHistoryMapper generationHistoryMapper;

    public LibraryService(InspirationMapper inspirationMapper,
                          GenerationHistoryMapper generationHistoryMapper) {
        this.inspirationMapper = inspirationMapper;
        this.generationHistoryMapper = generationHistoryMapper;
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

        Set<Long> historyIds = inspirations.stream().map(Inspiration::getHistoryId).collect(Collectors.toSet());
        if (historyIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, GenerationHistory> historyMap = generationHistoryMapper.selectBatchIds(historyIds).stream()
            .collect(Collectors.toMap(GenerationHistory::getId, Function.identity()));

        return inspirations.stream()
            .map(item -> toResponse(item, historyMap.get(item.getHistoryId())))
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

    private LibraryItemResponse toResponse(Inspiration inspiration, GenerationHistory history) {
        LibraryItemResponse response = new LibraryItemResponse();
        response.setId(inspiration.getId());
        response.setHistoryId(inspiration.getHistoryId());
        response.setCustomTags(inspiration.getCustomTags());
        if (history != null) {
            response.setCoreInput(history.getCoreInput());
            response.setStyle(history.getStyle());
            response.setResults(history.getResults());
        }
        response.setCreatedAt(inspiration.getCreatedAt() == null ? null :
            inspiration.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return response;
    }
}
