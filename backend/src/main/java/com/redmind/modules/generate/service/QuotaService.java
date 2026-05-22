package com.redmind.modules.generate.service;

import com.redmind.common.enums.MemberType;
import com.redmind.common.exception.BizException;
import com.redmind.modules.user.entity.User;
import com.redmind.modules.user.mapper.UserMapper;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class QuotaService {

    private final UserMapper userMapper;

    public QuotaService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void checkAndConsume(Long userId) {
        if (userId == null) {
            return;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        LocalDate today = LocalDate.now();
        if (user.getLastGenDate() == null || !today.equals(user.getLastGenDate())) {
            user.setDailyGenCount(0);
            user.setLastGenDate(today);
        }

        boolean isPro = MemberType.PRO.name().equalsIgnoreCase(user.getMemberType())
            && user.getMemberExpireAt() != null
            && user.getMemberExpireAt().toLocalDate().compareTo(today) >= 0;
        int limit = isPro ? Integer.MAX_VALUE : 10;
        if (user.getDailyGenCount() >= limit) {
            throw new BizException("今日生成次数已用尽");
        }

        user.setDailyGenCount(user.getDailyGenCount() + 1);
        userMapper.updateById(user);
    }
}
