package com.redmind.modules.generate.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.modules.generate.dto.AiRuntimeInfoResponse;
import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GenerateResponse;
import com.redmind.modules.generate.dto.GeneratedVersion;
import com.redmind.modules.generate.dto.OptimizeRequest;
import com.redmind.modules.generate.dto.OptimizeResponse;
import com.redmind.modules.generate.dto.VersionReviewRequest;
import com.redmind.modules.generate.service.CopyOptimizationService;
import com.redmind.modules.generate.service.GenerationService;
import com.redmind.modules.generate.service.SseGenerationService;
import com.redmind.modules.generate.service.VersionReviewService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/generate")
public class GenerateController {

    private final GenerationService generationService;
    private final SseGenerationService sseGenerationService;
    private final CopyOptimizationService copyOptimizationService;
    private final VersionReviewService versionReviewService;

    public GenerateController(GenerationService generationService,
                              SseGenerationService sseGenerationService,
                              CopyOptimizationService copyOptimizationService,
                              VersionReviewService versionReviewService) {
        this.generationService = generationService;
        this.sseGenerationService = sseGenerationService;
        this.copyOptimizationService = copyOptimizationService;
        this.versionReviewService = versionReviewService;
    }

    @PostMapping
    public ApiResponse<GenerateResponse> generate(@Valid @RequestBody GenerateRequest request) {
        return ApiResponse.success(generationService.generate(request));
    }

    @GetMapping("/runtime-info")
    public ApiResponse<AiRuntimeInfoResponse> runtimeInfo() {
        return ApiResponse.success(generationService.runtimeInfo());
    }

    @PostMapping("/stream")
    public SseEmitter stream(@Valid @RequestBody GenerateRequest request) {
        return sseGenerationService.streamGenerate(request);
    }

    @PostMapping("/optimize")
    public ApiResponse<OptimizeResponse> optimize(@Valid @RequestBody OptimizeRequest request) {
        return ApiResponse.success(copyOptimizationService.optimize(request));
    }

    @PostMapping("/review")
    public ApiResponse<GeneratedVersion> review(@Valid @RequestBody VersionReviewRequest request) {
        return ApiResponse.success(versionReviewService.review(request.getRequest(), request.getVersion()));
    }
}
