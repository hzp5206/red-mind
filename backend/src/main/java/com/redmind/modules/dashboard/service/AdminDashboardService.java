package com.redmind.modules.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redmind.common.enums.UserRole;
import com.redmind.modules.admin.entity.OperationLog;
import com.redmind.modules.admin.mapper.OperationLogMapper;
import com.redmind.modules.dashboard.dto.AdminDashboardResponse;
import com.redmind.modules.generate.entity.GenerationHistory;
import com.redmind.modules.generate.mapper.GenerationHistoryMapper;
import com.redmind.modules.moderation.entity.SensitiveWord;
import com.redmind.modules.moderation.mapper.SensitiveWordMapper;
import com.redmind.modules.template.mapper.TemplateMapper;
import com.redmind.modules.user.mapper.UserMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private final TemplateMapper templateMapper;
    private final UserMapper userMapper;
    private final GenerationHistoryMapper generationHistoryMapper;
    private final SensitiveWordMapper sensitiveWordMapper;
    private final OperationLogMapper operationLogMapper;

    public AdminDashboardService(TemplateMapper templateMapper,
                                 UserMapper userMapper,
                                 GenerationHistoryMapper generationHistoryMapper,
                                 SensitiveWordMapper sensitiveWordMapper,
                                 OperationLogMapper operationLogMapper) {
        this.templateMapper = templateMapper;
        this.userMapper = userMapper;
        this.generationHistoryMapper = generationHistoryMapper;
        this.sensitiveWordMapper = sensitiveWordMapper;
        this.operationLogMapper = operationLogMapper;
    }

    public AdminDashboardResponse summary() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return AdminDashboardResponse.builder()
            .templateCount(templateMapper.selectCount(null))
            .userCount(userMapper.selectCount(null))
            .adminCount(userMapper.selectCount(new LambdaQueryWrapper<com.redmind.modules.user.entity.User>()
                .eq(com.redmind.modules.user.entity.User::getRole, UserRole.ADMIN.name())))
            .todayGenerationCount(generationHistoryMapper.selectCount(
                new LambdaQueryWrapper<GenerationHistory>().ge(GenerationHistory::getCreatedAt, todayStart)))
            .sensitiveWordCount(sensitiveWordMapper.selectCount(
                new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getIsActive, true)))
            .todayOperationCount(operationLogMapper.selectCount(
                new LambdaQueryWrapper<OperationLog>().ge(OperationLog::getCreatedAt, todayStart)))
            .build();
    }
}
