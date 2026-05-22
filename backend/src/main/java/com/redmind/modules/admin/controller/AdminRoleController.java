package com.redmind.modules.admin.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.admin.dto.AdminPermissionOptionResponse;
import com.redmind.modules.admin.dto.AdminRoleResponse;
import com.redmind.modules.admin.dto.AdminRoleSaveRequest;
import com.redmind.modules.admin.service.AdminPermissionService;
import com.redmind.modules.admin.service.AdminRoleService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/roles")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;
    private final AdminPermissionService adminPermissionService;
    private final AdminGuard adminGuard;

    public AdminRoleController(AdminRoleService adminRoleService,
                               AdminPermissionService adminPermissionService,
                               AdminGuard adminGuard) {
        this.adminRoleService = adminRoleService;
        this.adminPermissionService = adminPermissionService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    public ApiResponse<List<AdminRoleResponse>> listRoles() {
        adminGuard.check(AdminPermission.ROLE_MANAGE);
        return ApiResponse.success(adminRoleService.listRoles());
    }

    @GetMapping("/assignable")
    public ApiResponse<List<AdminRoleResponse>> assignableRoles() {
        adminGuard.check(AdminPermission.USER_MANAGE);
        return ApiResponse.success(adminRoleService.listActiveRoles());
    }

    @GetMapping("/permissions")
    public ApiResponse<List<AdminPermissionOptionResponse>> listPermissions() {
        adminGuard.check(AdminPermission.ROLE_MANAGE);
        return ApiResponse.success(adminPermissionService.listAllPermissions());
    }

    @PostMapping
    public ApiResponse<AdminRoleResponse> save(@Valid @RequestBody AdminRoleSaveRequest request) {
        adminGuard.check(AdminPermission.ROLE_MANAGE);
        return ApiResponse.success(adminRoleService.save(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminGuard.check(AdminPermission.ROLE_MANAGE);
        adminRoleService.delete(id);
        return ApiResponse.success(null);
    }
}
