package com.redmind.modules.setting.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.setting.dto.AiConnectivityTestResponse;
import com.redmind.modules.setting.dto.AiSettingResponse;
import com.redmind.modules.setting.dto.AiSettingSaveRequest;
import com.redmind.modules.setting.service.AiSettingService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai-settings")
public class AdminAiSettingController {

    private final AiSettingService aiSettingService;
    private final AdminGuard adminGuard;

    public AdminAiSettingController(AiSettingService aiSettingService, AdminGuard adminGuard) {
        this.aiSettingService = aiSettingService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    public ApiResponse<AiSettingResponse> getCurrent() {
        adminGuard.check(AdminPermission.AI_SETTING_MANAGE);
        return ApiResponse.success(aiSettingService.getCurrent());
    }

    @PostMapping
    public ApiResponse<AiSettingResponse> save(@Valid @RequestBody AiSettingSaveRequest request) {
        adminGuard.check(AdminPermission.AI_SETTING_MANAGE);
        return ApiResponse.success(aiSettingService.save(request));
    }

    @PostMapping("/test")
    public ApiResponse<AiConnectivityTestResponse> test() {
        adminGuard.check(AdminPermission.AI_SETTING_MANAGE);
        return ApiResponse.success(aiSettingService.testConnectivity());
    }
}
