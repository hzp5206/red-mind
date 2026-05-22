package com.redmind.modules.user.service;

import com.redmind.common.enums.MemberType;
import com.redmind.common.enums.UserRole;
import com.redmind.common.exception.BizException;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.JwtUserContext;
import com.redmind.modules.admin.service.OperationLogService;
import com.redmind.modules.user.dto.AdminUserOverviewResponse;
import com.redmind.modules.user.dto.UserManageRequest;
import com.redmind.modules.user.dto.UserProfileResponse;
import com.redmind.modules.user.entity.User;
import com.redmind.modules.user.mapper.UserMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
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

    public List<AdminUserOverviewResponse> listUsers(String memberType) {
        return userMapper.selectList(null).stream()
            .filter(user -> memberType == null || memberType.isEmpty() || memberType.equalsIgnoreCase(user.getMemberType()))
            .map(user -> AdminUserOverviewResponse.builder()
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
    }

    public void updateUser(Long id, UserManageRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
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
        operationLogService.log(
            "user",
            "update",
            "user",
            user.getId(),
            "调整用户[" + user.getNickname() + "]角色/会员/到期时间/使用次数"
        );
    }
}
