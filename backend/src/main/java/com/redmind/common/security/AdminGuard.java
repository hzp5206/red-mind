package com.redmind.common.security;

import com.redmind.common.enums.UserRole;
import com.redmind.common.exception.BizException;
import com.redmind.modules.admin.service.AdminPermissionService;
import com.redmind.modules.user.entity.User;
import com.redmind.modules.user.mapper.UserMapper;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AdminGuard {

    private final UserMapper userMapper;
    private final AdminPermissionService adminPermissionService;

    public AdminGuard(UserMapper userMapper, AdminPermissionService adminPermissionService) {
        this.userMapper = userMapper;
        this.adminPermissionService = adminPermissionService;
    }

    public void checkAdmin() {
        User user = requireCurrentUser();
        if (!UserRole.ADMIN.name().equalsIgnoreCase(user.getRole())) {
            throw new BizException("无管理员权限");
        }
    }

    public void check(String permissionCode) {
        User user = requireCurrentUser();
        if (!UserRole.ADMIN.name().equalsIgnoreCase(user.getRole())) {
            throw new BizException("无管理员权限");
        }
        String roleCode = user.getRoleCode() == null || user.getRoleCode().trim().isEmpty() ? user.getRole() : user.getRoleCode();
        Set<String> permissions = adminPermissionService.getPermissionsByRole(roleCode);
        if (!permissions.contains(permissionCode)) {
            throw new BizException("暂无该功能权限");
        }
    }

    public Set<String> currentPermissions() {
        User user = requireCurrentUser();
        if (!UserRole.ADMIN.name().equalsIgnoreCase(user.getRole())) {
            return java.util.Collections.emptySet();
        }
        String roleCode = user.getRoleCode() == null || user.getRoleCode().trim().isEmpty() ? user.getRole() : user.getRoleCode();
        return adminPermissionService.getPermissionsByRole(roleCode);
    }

    private User requireCurrentUser() {
        Long userId = JwtUserContext.getUserId();
        if (userId == null) {
            throw new BizException("请先登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }
}
