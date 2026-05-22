package com.redmind.modules.auth.service;

import com.redmind.common.enums.MemberType;
import com.redmind.common.enums.UserRole;
import com.redmind.common.exception.BizException;
import com.redmind.common.security.JwtTokenUtil;
import com.redmind.modules.admin.service.AdminPermissionService;
import com.redmind.modules.auth.dto.LoginRequest;
import com.redmind.modules.auth.dto.RegisterRequest;
import com.redmind.modules.auth.dto.TokenResponse;
import com.redmind.modules.user.entity.User;
import com.redmind.modules.user.mapper.UserMapper;
import java.time.LocalDate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AdminPermissionService adminPermissionService;

    public AuthService(UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       JwtTokenUtil jwtTokenUtil,
                       AdminPermissionService adminPermissionService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.adminPermissionService = adminPermissionService;
    }

    public TokenResponse register(RegisterRequest request) {
        User existing = userMapper.findByEmail(request.getEmail());
        if (existing != null) {
            throw new BizException("该邮箱已注册");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER.name());
        user.setRoleCode(UserRole.USER.name());
        user.setMemberType(MemberType.FREE.name());
        user.setDailyGenCount(0);
        user.setLastGenDate(LocalDate.now());
        userMapper.insert(user);

        String token = jwtTokenUtil.generateToken(user.getId());
        return new TokenResponse(
            user.getId(),
            token,
            user.getNickname(),
            user.getRole(),
            user.getRoleCode(),
            adminPermissionService.getPermissionsByRole(user.getRoleCode())
        );
    }

    public TokenResponse login(LoginRequest request) {
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException("邮箱或密码错误");
        }
        String token = jwtTokenUtil.generateToken(user.getId());
        return new TokenResponse(
            user.getId(),
            token,
            user.getNickname(),
            user.getRole(),
            user.getRoleCode() == null ? user.getRole() : user.getRoleCode(),
            adminPermissionService.getPermissionsByRole(user.getRoleCode() == null ? user.getRole() : user.getRoleCode())
        );
    }
}
