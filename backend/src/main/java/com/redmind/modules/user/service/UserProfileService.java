package com.redmind.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.redmind.common.enums.MemberType;
import com.redmind.common.enums.UserRole;
import com.redmind.common.exception.BizException;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.JwtUserContext;
import com.redmind.modules.admin.service.OperationLogService;
import com.redmind.modules.common.dto.PageResponse;
import com.redmind.modules.user.dto.AdminUserExportItem;
import com.redmind.modules.user.dto.AdminUserQueryRequest;
import com.redmind.modules.user.dto.AdminUserOverviewResponse;
import com.redmind.modules.user.dto.UserManageRequest;
import com.redmind.modules.user.dto.UserProfileResponse;
import com.redmind.modules.user.entity.User;
import com.redmind.modules.user.mapper.UserMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserMapper userMapper;
    private final AdminGuard adminGuard;
    private final OperationLogService operationLogService;

    public UserProfileService(UserMapper userMapper,
                              AdminGuard adminGuard,
                              OperationLogService operationLogService) {
        this.userMapper = userMapper;
        this.adminGuard = adminGuard;
        this.operationLogService = operationLogService;
    }

    public UserProfileResponse currentUser() {
        Long userId = JwtUserContext.getUserId();
        if (userId == null) {
            throw new BizException("请先登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        boolean newDay = user.getLastGenDate() == null || !LocalDate.now().equals(user.getLastGenDate());
        int used = newDay ? 0 : user.getDailyGenCount();
        boolean isPro = MemberType.PRO.name().equalsIgnoreCase(user.getMemberType())
            && user.getMemberExpireAt() != null
            && user.getMemberExpireAt().toLocalDate().compareTo(LocalDate.now()) >= 0;
        int limit = isPro ? -1 : 10;
        int remaining = isPro ? -1 : Math.max(limit - used, 0);

        return UserProfileResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .role(user.getRole())
            .roleCode(user.getRoleCode())
            .memberType(user.getMemberType())
            .dailyGenCount(used)
            .dailyLimit(limit)
            .remainingCount(remaining)
            .pro(isPro)
            .permissions(adminGuard.currentPermissions())
            .build();
    }

    public PageResponse<AdminUserOverviewResponse> listUsers(AdminUserQueryRequest request, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<User> wrapper = buildUserQuery(request).orderByDesc(User::getId);
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userMapper.selectPage(page, wrapper);
        return new PageResponse<>(
            result.getTotal(),
            result.getRecords().stream().map(user -> AdminUserOverviewResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .roleCode(user.getRoleCode())
                .memberType(user.getMemberType())
                .dailyGenCount(user.getDailyGenCount())
                .lastGenDate(user.getLastGenDate() == null ? null : user.getLastGenDate().toString())
                .build())
                .collect(Collectors.toList())
        );
    }

    public List<AdminUserExportItem> exportUsers(AdminUserQueryRequest request) {
        List<AdminUserExportItem> items = userMapper.selectList(buildUserQuery(request).orderByDesc(User::getId))
            .stream()
            .map(user -> AdminUserExportItem.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .roleCode(user.getRoleCode())
                .memberType(user.getMemberType())
                .dailyGenCount(user.getDailyGenCount())
                .lastGenDate(user.getLastGenDate() == null ? null : user.getLastGenDate().toString())
                .build())
            .collect(Collectors.toList());
        operationLogService.log(
            "user",
            "export",
            "user",
            null,
            "导出用户列表",
            null,
            "keyword=" + (request == null ? "" : request.getKeyword())
                + ", memberType=" + (request == null ? "" : request.getMemberType())
                + ", role=" + (request == null ? "" : request.getRole())
                + ", roleCode=" + (request == null ? "" : request.getRoleCode())
        );
        return items;
    }

    public void updateUser(Long id, UserManageRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        String before = "role=" + user.getRole()
            + ", roleCode=" + user.getRoleCode()
            + ", memberType=" + user.getMemberType()
            + ", memberExpireAt=" + user.getMemberExpireAt()
            + ", dailyGenCount=" + user.getDailyGenCount();
        if (request.getMemberType() != null) {
            user.setMemberType(request.getMemberType());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
            if (UserRole.USER.name().equalsIgnoreCase(request.getRole())) {
                user.setRoleCode(UserRole.USER.name());
            }
            if (UserRole.ADMIN.name().equalsIgnoreCase(request.getRole())
                && (request.getRoleCode() == null || request.getRoleCode().trim().isEmpty())) {
                user.setRoleCode(UserRole.ADMIN.name());
            }
        }
        if (request.getRoleCode() != null && !request.getRoleCode().trim().isEmpty()) {
            user.setRoleCode(request.getRoleCode());
        }
        if (request.getMemberExpireAt() != null) {
            user.setMemberExpireAt(request.getMemberExpireAt());
        }
        if (request.getDailyGenCount() != null) {
            user.setDailyGenCount(request.getDailyGenCount());
        }
        userMapper.updateById(user);
        String after = "role=" + user.getRole()
            + ", roleCode=" + user.getRoleCode()
            + ", memberType=" + user.getMemberType()
            + ", memberExpireAt=" + user.getMemberExpireAt()
            + ", dailyGenCount=" + user.getDailyGenCount();
        operationLogService.log(
            "user",
            "update",
            "user",
            user.getId(),
            "调整用户[" + user.getNickname() + "]角色/会员/到期时间/使用次数",
            before,
            after
        );
    }

    private LambdaQueryWrapper<User> buildUserQuery(AdminUserQueryRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (request != null) {
            if (StringUtils.isNotBlank(request.getMemberType())) {
                wrapper.eq(User::getMemberType, request.getMemberType());
            }
            if (StringUtils.isNotBlank(request.getRole())) {
                wrapper.eq(User::getRole, request.getRole());
            }
            if (StringUtils.isNotBlank(request.getRoleCode())) {
                wrapper.eq(User::getRoleCode, request.getRoleCode());
            }
            if (StringUtils.isNotBlank(request.getKeyword())) {
                wrapper.and(query -> query
                    .like(User::getEmail, request.getKeyword())
                    .or()
                    .like(User::getNickname, request.getKeyword()));
            }
        }
        return wrapper;
    }
}
