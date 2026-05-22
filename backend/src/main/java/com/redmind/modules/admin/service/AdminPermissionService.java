package com.redmind.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redmind.modules.admin.dto.AdminPermissionOptionResponse;
import com.redmind.modules.admin.entity.AdminPermissionEntity;
import com.redmind.modules.admin.entity.AdminRolePermission;
import com.redmind.modules.admin.mapper.AdminPermissionMapper;
import com.redmind.modules.admin.mapper.AdminRolePermissionMapper;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class AdminPermissionService {

    private final AdminRolePermissionMapper adminRolePermissionMapper;
    private final AdminPermissionMapper adminPermissionMapper;

    public AdminPermissionService(AdminRolePermissionMapper adminRolePermissionMapper,
                                  AdminPermissionMapper adminPermissionMapper) {
        this.adminRolePermissionMapper = adminRolePermissionMapper;
        this.adminPermissionMapper = adminPermissionMapper;
    }

    public Set<String> getPermissionsByRole(String roleCode) {
        if (StringUtils.isBlank(roleCode)) {
            return Collections.emptySet();
        }
        List<AdminRolePermission> mappings = adminRolePermissionMapper.selectList(
            new LambdaQueryWrapper<AdminRolePermission>().eq(AdminRolePermission::getRoleCode, roleCode)
        );
        return mappings.stream().map(AdminRolePermission::getPermissionCode).collect(Collectors.toSet());
    }

    public List<AdminPermissionOptionResponse> listAllPermissions() {
        return adminPermissionMapper.selectList(new LambdaQueryWrapper<AdminPermissionEntity>()
                .orderByAsc(AdminPermissionEntity::getModuleName)
                .orderByAsc(AdminPermissionEntity::getId))
            .stream()
            .map(item -> AdminPermissionOptionResponse.builder()
                .permissionCode(item.getPermissionCode())
                .permissionName(item.getPermissionName())
                .moduleName(item.getModuleName())
                .build())
            .collect(Collectors.toList());
    }
}
