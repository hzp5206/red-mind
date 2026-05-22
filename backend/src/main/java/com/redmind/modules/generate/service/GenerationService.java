package com.redmind.modules.generate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmind.common.security.JwtUserContext;
import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GenerateResponse;
import com.redmind.modules.generate.dto.GeneratedVersion;
import com.redmind.modules.generate.entity.GenerationHistory;
import com.redmind.modules.generate.mapper.GenerationHistoryMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GenerationService {

    private final AiProviderRouter aiProviderRouter;
    private final QualityScoreService qualityScoreService;
    private final GenerationHistoryMapper generationHistoryMapper;
    private final QuotaService quotaService;
    private final ObjectMapper objectMapper;

    public GenerationService(AiProviderRouter aiProviderRouter,
                             QualityScoreService qualityScoreService,
                             GenerationHistoryMapper generationHistoryMapper,
                             QuotaService quotaService,
                             ObjectMapper objectMapper) {
        this.aiProviderRouter = aiProviderRouter;
        this.qualityScoreService = qualityScoreService;
        this.generationHistoryMapper = generationHistoryMapper;
        this.quotaService = quotaService;
        this.objectMapper = objectMapper;
    }

    public GenerateResponse generate(GenerateRequest request) {
        Long userId = JwtUserContext.getUserId();
        quotaService.checkAndConsume(userId);

        List<GeneratedVersion> versions = aiProviderRouter.route().generate(request);
        for (GeneratedVersion version : versions) {
            version.setQualityScores(qualityScoreService.score(request, version));
        }

        String generationId = "gen_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Long historyId = saveHistory(userId, request, versions);
        return new GenerateResponse(historyId, generationId, versions, 1);
    }

    private Long saveHistory(Long userId, GenerateRequest request, List<GeneratedVersion> versions) {
        try {
            GenerationHistory history = new GenerationHistory();
            history.setUserId(userId == null ? 0L : userId);
            history.setCoreInput(request.getCoreDescription());
            history.setStyle(request.getStyle());
            history.setPersona(request.getTone());
            history.setWordCount(request.getWordCount());
            history.setResults(objectMapper.writeValueAsString(versions));
            history.setIsCollected(false);
            history.setCreatedAt(LocalDateTime.now());
            generationHistoryMapper.insert(history);
            return history.getId();
        } catch (Exception ignored) {
            return null;
        }
    }
}
