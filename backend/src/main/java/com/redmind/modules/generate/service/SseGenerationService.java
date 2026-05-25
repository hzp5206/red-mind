package com.redmind.modules.generate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmind.common.exception.BizException;
import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GenerateResponse;
import com.redmind.modules.generate.dto.GeneratedVersion;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
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
        AtomicBoolean completed = new AtomicBoolean(false);

        emitter.onCompletion(() -> completed.set(true));
        emitter.onTimeout(() -> completed.set(true));
        emitter.onError((error) -> completed.set(true));

        executor.execute(() -> {
            try {
                GenerateResponse response = generationService.generate(request);
                List<GeneratedVersion> versions = response.getVersions();
                for (GeneratedVersion version : versions) {
                    if (completed.get()) {
                        return;
                    }
                    Map<String, Object> strategyPayload = new LinkedHashMap<>();
                    strategyPayload.put("ver", version.getVerNum());
                    strategyPayload.put("angleLabel", version.getAngleLabel());
                    strategyPayload.put("hookType", version.getHookType());
                    strategyPayload.put("strategySummary", version.getStrategySummary());
                    strategyPayload.put("opening", version.getOpening());
                    strategyPayload.put("cta", version.getCta());
                    sendJson(emitter, completed, "strategy", strategyPayload);
                    sendJson(emitter, completed, "version", version);
                }

                if (completed.get()) {
                    return;
                }
                Map<String, Object> donePayload = new LinkedHashMap<>();
                donePayload.put("generation_id", response.getGenerationId());
                donePayload.put("history_id", response.getHistoryId());
                donePayload.put("credits_used", response.getCreditsUsed());
                sendJson(emitter, completed, "done", donePayload);
                complete(emitter, completed);
            } catch (Exception exception) {
                sendErrorEvent(emitter, completed, resolveErrorMessage(exception));
                complete(emitter, completed);
            }
        });
        return emitter;
    }

    private void sendJson(SseEmitter emitter, AtomicBoolean completed, String eventName, Object payload) throws IOException {
        if (completed.get()) {
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                .name(eventName)
                .data(objectMapper.writeValueAsString(payload)));
        } catch (IllegalStateException exception) {
            completed.set(true);
        }
    }

    private void sendErrorEvent(SseEmitter emitter, AtomicBoolean completed, String message) {
        if (completed.get()) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
        } catch (IOException | IllegalStateException ignored) {
            completed.set(true);
        }
    }

    private String resolveErrorMessage(Exception exception) {
        if (exception instanceof BizException) {
            return exception.getMessage();
        }
        if (exception instanceof ResourceAccessException) {
            return "AI 服务连接超时，请检查 Base URL、网络代理或目标模型服务状态";
        }
        return "流式生成失败，请稍后重试";
    }

    private void complete(SseEmitter emitter, AtomicBoolean completed) {
        if (completed.compareAndSet(false, true)) {
            emitter.complete();
        }
    }
}
