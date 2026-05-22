package com.redmind.modules.user.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.user.dto.AdminUserOverviewResponse;
import com.redmind.modules.user.dto.UserManageRequest;
import com.redmind.modules.user.dto.UserProfileResponse;
import com.redmind.modules.user.service.UserProfileService;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserProfileService userProfileService;
    private final AdminGuard adminGuard;

    public UserController(UserProfileService userProfileService, AdminGuard adminGuard) {
        this.userProfileService = userProfileService;
        this.adminGuard = adminGuard;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.success(userProfileService.currentUser());
    }

    @GetMapping("/admin")
    public ApiResponse<List<AdminUserOverviewResponse>> admin(@RequestParam(required = false) String memberType) {
        adminGuard.check(AdminPermission.USER_MANAGE);
        return ApiResponse.success(userProfileService.listUsers(memberType));
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody UserManageRequest request) {
        adminGuard.check(AdminPermission.USER_MANAGE);
        userProfileService.updateUser(id, request);
        return ApiResponse.success(null);
    }
}
