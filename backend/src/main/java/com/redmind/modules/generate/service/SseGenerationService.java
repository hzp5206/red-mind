package com.redmind.modules.generate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GenerateResponse;
import com.redmind.modules.generate.dto.GeneratedVersion;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseGenerationService {

    private final GenerationService generationService;
    private final ObjectMapper objectMapper;
    private final Executor executor = Executors.newCachedThreadPool();

    public SseGenerationService(GenerationService generationService, ObjectMapper objectMapper) {
        this.generationService = generationService;
        this.objectMapper = objectMapper;
    }

    public SseEmitter streamGenerate(GenerateRequest request) {
        SseEmitter emitter = new SseEmitter(60000L);
        executor.execute(() -> {
            try {
                GenerateResponse response = generationService.generate(request);
                List<GeneratedVersion> versions = response.getVersions();
                for (GeneratedVersion version : versions) {
                    Map<String, Object> strategyPayload = new LinkedHashMap<>();
                    strategyPayload.put("ver", version.getVerNum());
                    strategyPayload.put("angleLabel", version.getAngleLabel());
                    strategyPayload.put("hookType", version.getHookType());
                    strategyPayload.put("strategySummary", version.getStrategySummary());
                    strategyPayload.put("opening", version.getOpening());
                    strategyPayload.put("cta", version.getCta());
                    sendJson(emitter, "strategy", strategyPayload);
                    sendJson(emitter, "version", version);
                }

                Map<String, Object> donePayload = new LinkedHashMap<>();
                donePayload.put("generation_id", response.getGenerationId());
                donePayload.put("history_id", response.getHistoryId());
                donePayload.put("credits_used", response.getCreditsUsed());
                sendJson(emitter, "done", donePayload);
                emitter.complete();
            } catch (Exception exception) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(exception.getMessage()));
                } catch (IOException ignored) {
                }
                emitter.completeWithError(exception);
            }
        });
        return emitter;
    }

    private void sendJson(SseEmitter emitter, String eventName, Object payload) throws IOException {
        emitter.send(SseEmitter.event()
            .name(eventName)
            .data(objectMapper.writeValueAsString(payload)));
    }
}
