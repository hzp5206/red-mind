package com.redmind.modules.user.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.common.dto.PageResponse;
import com.redmind.modules.user.dto.AdminUserExportItem;
import com.redmind.modules.user.dto.AdminUserQueryRequest;
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
    public ApiResponse<PageResponse<AdminUserOverviewResponse>> admin(AdminUserQueryRequest request,
                                                                      @RequestParam(defaultValue = "1") Integer page,
                                                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        adminGuard.check(AdminPermission.USER_MANAGE);
        return ApiResponse.success(userProfileService.listUsers(request, page, pageSize));
    }

    @GetMapping("/admin/export")
    public ApiResponse<String> export(AdminUserQueryRequest request) {
        adminGuard.check(AdminPermission.USER_MANAGE);
        List<AdminUserExportItem> records = userProfileService.exportUsers(request);
        StringBuilder builder = new StringBuilder("用户ID,邮箱,昵称,后台身份,后台角色,会员类型,今日使用次数,最近生成日期\n");
        for (AdminUserExportItem item : records) {
            builder.append(item.getId()).append(",")
                .append(safeCsv(item.getEmail())).append(",")
                .append(safeCsv(item.getNickname())).append(",")
                .append(safeCsv(item.getRole())).append(",")
                .append(safeCsv(item.getRoleCode())).append(",")
                .append(safeCsv(item.getMemberType())).append(",")
                .append(item.getDailyGenCount() == null ? "" : item.getDailyGenCount()).append(",")
                .append(safeCsv(item.getLastGenDate()))
                .append("\n");
        }
        return ApiResponse.success(builder.toString());
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody UserManageRequest request) {
        adminGuard.check(AdminPermission.USER_MANAGE);
        userProfileService.updateUser(id, request);
        return ApiResponse.success(null);
    }

    private String safeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
