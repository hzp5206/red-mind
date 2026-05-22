package com.redmind.modules.log.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.common.dto.PageResponse;
import com.redmind.modules.log.dto.GenerationLogQueryRequest;
import com.redmind.modules.log.dto.GenerationLogResponse;
import com.redmind.modules.log.service.GenerationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/logs")
public class GenerationLogController {

    private final GenerationLogService generationLogService;
    private final AdminGuard adminGuard;

    public GenerationLogController(GenerationLogService generationLogService, AdminGuard adminGuard) {
        this.generationLogService = generationLogService;
        this.adminGuard = adminGuard;
    }

    @GetMapping("/generations")
    public ApiResponse<PageResponse<GenerationLogResponse>> generations(GenerationLogQueryRequest request,
                                                                        @RequestParam(defaultValue = "1") Integer page,
                                                                        @RequestParam(defaultValue = "20") Integer pageSize) {
        adminGuard.check(AdminPermission.GENERATION_LOG_VIEW);
        return ApiResponse.success(generationLogService.page(request, page, pageSize));
    }
}
