package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GenerateResponse;
import com.redmind.modules.generate.dto.GeneratedVersion;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseGenerationService {

    private final GenerationService generationService;
    private final Executor executor = Executors.newCachedThreadPool();

    public SseGenerationService(GenerationService generationService) {
        this.generationService = generationService;
    }

    public SseEmitter streamGenerate(GenerateRequest request) {
        SseEmitter emitter = new SseEmitter(60000L);
        executor.execute(() -> {
            try {
                GenerateResponse response = generationService.generate(request);
                List<GeneratedVersion> versions = response.getVersions();
                for (GeneratedVersion version : versions) {
                    emitter.send(SseEmitter.event()
                        .name("title")
                        .data("{\"ver\":" + version.getVerNum() + ",\"text\":\"" + escape(version.getTitle()) + "\"}"));
                    emitter.send(SseEmitter.event()
                        .name("content")
                        .data("{\"ver\":" + version.getVerNum() + ",\"text\":\"" + escape(version.getContent()) + "\"}"));
                    emitter.send(SseEmitter.event()
                        .name("tags")
                        .data("{\"ver\":" + version.getVerNum() + ",\"text\":\"" + escape(String.join(",", version.getTags())) + "\"}"));
                }
                emitter.send(SseEmitter.event()
                    .name("done")
                    .data("{\"generation_id\":\"" + response.getGenerationId()
                        + "\",\"history_id\":" + response.getHistoryId() + ",\"credits_used\":1}"));
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

    private String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
