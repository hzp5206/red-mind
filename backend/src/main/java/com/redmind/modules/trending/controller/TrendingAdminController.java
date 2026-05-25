package com.redmind.modules.trending.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.common.dto.PageResponse;
import com.redmind.modules.trending.dto.TrendingAnalysisResponse;
import com.redmind.modules.trending.dto.TrendingDashboardResponse;
import com.redmind.modules.trending.dto.TrendingItemResponse;
import com.redmind.modules.trending.dto.TrendingTaskResponse;
import com.redmind.modules.trending.dto.TrendingTaskSaveRequest;
import com.redmind.modules.trending.service.TrendingAdminService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/trending-copies")
public class TrendingAdminController {

    private final TrendingAdminService trendingAdminService;
    private final AdminGuard adminGuard;

    public TrendingAdminController(TrendingAdminService trendingAdminService, AdminGuard adminGuard) {
        this.trendingAdminService = trendingAdminService;
        this.adminGuard = adminGuard;
    }

    @GetMapping("/dashboard")
    public ApiResponse<TrendingDashboardResponse> dashboard() {
        adminGuard.check(AdminPermission.TRENDING_COPY_MANAGE);
        return ApiResponse.success(trendingAdminService.dashboard());
    }

    @GetMapping("/tasks")
    public ApiResponse<List<TrendingTaskResponse>> tasks() {
        adminGuard.check(AdminPermission.TRENDING_COPY_MANAGE);
        return ApiResponse.success(trendingAdminService.listTasks());
    }

    @PostMapping("/tasks")
    public ApiResponse<TrendingTaskResponse> saveTask(@Valid @RequestBody TrendingTaskSaveRequest request) {
        adminGuard.check(AdminPermission.TRENDING_COPY_MANAGE);
        return ApiResponse.success(trendingAdminService.saveTask(request));
    }

    @DeleteMapping("/tasks/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        adminGuard.check(AdminPermission.TRENDING_COPY_MANAGE);
        trendingAdminService.deleteTask(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/tasks/{id}/trigger")
    public ApiResponse<Integer> trigger(@PathVariable Long id) {
        adminGuard.check(AdminPermission.TRENDING_COPY_MANAGE);
        return ApiResponse.success(trendingAdminService.trigger(id));
    }

    @GetMapping("/items")
    public ApiResponse<PageResponse<TrendingItemResponse>> pageItems(@RequestParam(required = false) String keyword,
                                                                    @RequestParam(required = false) String platformCode,
                                                                    @RequestParam(defaultValue = "1") Integer page,
                                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        adminGuard.check(AdminPermission.TRENDING_COPY_MANAGE);
        return ApiResponse.success(trendingAdminService.pageItems(keyword, platformCode, page, pageSize));
    }

    @GetMapping("/items/{id}/analysis")
    public ApiResponse<TrendingAnalysisResponse> analysis(@PathVariable Long id) {
        adminGuard.check(AdminPermission.TRENDING_COPY_MANAGE);
        return ApiResponse.success(trendingAdminService.analyzeItem(id));
    }

    @PostMapping("/items/{id}/collect")
    public ApiResponse<Void> collect(@PathVariable Long id) {
        adminGuard.check(AdminPermission.TRENDING_COPY_MANAGE);
        trendingAdminService.collectItem(id);
        return ApiResponse.success(null);
    }
}
