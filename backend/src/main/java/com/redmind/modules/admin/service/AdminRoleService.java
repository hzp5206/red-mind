package com.redmind.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redmind.common.enums.UserRole;
import com.redmind.common.exception.BizException;
import com.redmind.modules.admin.dto.AdminRoleResponse;
import com.redmind.modules.admin.dto.AdminRoleSaveRequest;
import com.redmind.modules.admin.entity.AdminRole;
import com.redmind.modules.admin.entity.AdminRolePermission;
import com.redmind.modules.admin.mapper.AdminRoleMapper;
import com.redmind.modules.admin.mapper.AdminRolePermissionMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminRoleService {

    private final AdminRoleMapper adminRoleMapper;
    private final AdminRolePermissionMapper adminRolePermissionMapper;
    private final OperationLogService operationLogService;
    private final AdminPermissionService adminPermissionService;

    public AdminRoleService(AdminRoleMapper adminRoleMapper,
                            AdminRolePermissionMapper adminRolePermissionMapper,
                            OperationLogService operationLogService,
                            AdminPermissionService adminPermissionService) {
        this.adminRoleMapper = adminRoleMapper;
        this.adminRolePermissionMapper = adminRolePermissionMapper;
        this.operationLogService = operationLogService;
        this.adminPermissionService = adminPermissionService;
    }

    public List<AdminRoleResponse> listRoles() {
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
                .orderByDesc(AdminRole::getIsActive)
                .orderByAsc(AdminRole::getId))
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<AdminRoleResponse> listActiveRoles() {
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
                .eq(AdminRole::getIsActive, true)
                .orderByAsc(AdminRole::getId))
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public AdminRoleResponse save(AdminRoleSaveRequest request) {
        AdminRole role = request.getId() == null ? new AdminRole() : adminRoleMapper.selectById(request.getId());
        if (role == null) {
            throw new BizException("角色不存在");
        }
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescriptionText(request.getDescriptionText());
        role.setIsActive(request.getIsActive() == null ? true : request.getIsActive());
        if (role.getId() == null) {
            role.setCreatedAt(LocalDateTime.now());
            adminRoleMapper.insert(role);
            operationLogService.log("role", "create", "admin_role", role.getId(), "新增角色：" + role.getRoleName());
        } else {
            adminRoleMapper.updateById(role);
            operationLogService.log("role", "update", "admin_role", role.getId(), "更新角色：" + role.getRoleName());
        }

        adminRolePermissionMapper.delete(new LambdaQueryWrapper<AdminRolePermission>()
            .eq(AdminRolePermission::getRoleCode, role.getRoleCode()));
        if (request.getPermissions() != null) {
            for (String permissionCode : request.getPermissions()) {
                AdminRolePermission mapping = new AdminRolePermission();
                mapping.setRoleCode(role.getRoleCode());
                mapping.setPermissionCode(permissionCode);
                mapping.setCreatedAt(LocalDateTime.now());
                adminRolePermissionMapper.insert(mapping);
            }
        }
        operationLogService.log("role", "grant", "admin_role", role.getId(), "更新角色权限：" + role.getRoleCode());
        return toResponse(role);
    }

    public void delete(Long id) {
        AdminRole role = adminRoleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        if (UserRole.ADMIN.name().equalsIgnoreCase(role.getRoleCode())) {
            throw new BizException("默认超级管理员角色不可删除");
        }
        adminRolePermissionMapper.delete(new LambdaQueryWrapper<AdminRolePermission>()
            .eq(AdminRolePermission::getRoleCode, role.getRoleCode()));
        adminRoleMapper.deleteById(id);
        operationLogService.log("role", "delete", "admin_role", id, "删除角色：" + role.getRoleName());
    }

    private AdminRoleResponse toResponse(AdminRole role) {
        Set<String> permissions = adminPermissionService.getPermissionsByRole(role.getRoleCode());
        return AdminRoleResponse.builder()
            .id(role.getId())
            .roleCode(role.getRoleCode())
            .roleName(role.getRoleName())
            .descriptionText(role.getDescriptionText())
            .isActive(role.getIsActive())
            .permissions(permissions == null ? Collections.emptyList() : permissions.stream().sorted().collect(Collectors.toList()))
            .build();
    }
}
