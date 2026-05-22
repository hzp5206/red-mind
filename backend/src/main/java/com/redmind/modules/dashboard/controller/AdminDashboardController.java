package com.redmind.modules.dashboard.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.dashboard.dto.AdminDashboardResponse;
import com.redmind.modules.dashboard.service.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final AdminGuard adminGuard;

    public AdminDashboardController(AdminDashboardService adminDashboardService, AdminGuard adminGuard) {
        this.adminDashboardService = adminDashboardService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    public ApiResponse<AdminDashboardResponse> summary() {
        adminGuard.check(AdminPermission.DASHBOARD_VIEW);
        return ApiResponse.success(adminDashboardService.summary());
    }
}
