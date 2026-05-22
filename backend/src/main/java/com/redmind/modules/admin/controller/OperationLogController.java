package com.redmind.modules.admin.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.admin.dto.OperationLogQueryRequest;
import com.redmind.modules.admin.dto.OperationLogResponse;
import com.redmind.modules.admin.service.OperationLogService;
import com.redmind.modules.common.dto.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/logs")
public class OperationLogController {

    private final OperationLogService operationLogService;
    private final AdminGuard adminGuard;

    public OperationLogController(OperationLogService operationLogService, AdminGuard adminGuard) {
        this.operationLogService = operationLogService;
        this.adminGuard = adminGuard;
    }

    @GetMapping("/operations")
    public ApiResponse<PageResponse<OperationLogResponse>> operations(OperationLogQueryRequest request,
                                                                      @RequestParam(defaultValue = "1") Integer page,
                                                                      @RequestParam(defaultValue = "20") Integer pageSize) {
        adminGuard.check(AdminPermission.OPERATION_LOG_VIEW);
        return ApiResponse.success(operationLogService.page(request, page, pageSize));
    }
}
