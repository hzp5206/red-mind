package com.redmind.modules.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redmind.common.enums.UserRole;
import com.redmind.modules.admin.entity.OperationLog;
import com.redmind.modules.admin.mapper.OperationLogMapper;
import com.redmind.modules.dashboard.dto.AdminDashboardResponse;
import com.redmind.modules.dashboard.dto.RecentOperationItem;
import com.redmind.modules.dashboard.dto.SummaryBucketItem;
import com.redmind.modules.generate.entity.GenerationHistory;
import com.redmind.modules.generate.mapper.GenerationHistoryMapper;
import com.redmind.modules.moderation.entity.SensitiveWord;
import com.redmind.modules.moderation.mapper.SensitiveWordMapper;
import com.redmind.modules.template.mapper.TemplateMapper;
import com.redmind.modules.user.mapper.UserMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        List<com.redmind.modules.user.entity.User> admins = userMapper.selectList(
            new LambdaQueryWrapper<com.redmind.modules.user.entity.User>().eq(com.redmind.modules.user.entity.User::getRole, UserRole.ADMIN.name())
        );
        List<OperationLog> operations = operationLogMapper.selectList(new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getId));
        return AdminDashboardResponse.builder()
            .templateCount(templateMapper.selectCount(null))
            .userCount(userMapper.selectCount(null))
            .adminCount((long) admins.size())
            .todayGenerationCount(generationHistoryMapper.selectCount(
                new LambdaQueryWrapper<GenerationHistory>().ge(GenerationHistory::getCreatedAt, todayStart)))
            .sensitiveWordCount(sensitiveWordMapper.selectCount(
                new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getIsActive, true)))
            .todayOperationCount(operationLogMapper.selectCount(
                new LambdaQueryWrapper<OperationLog>().ge(OperationLog::getCreatedAt, todayStart)))
            .recentOperations(operations.stream()
                .limit(6)
                .map(item -> RecentOperationItem.builder()
                    .id(item.getId())
                    .operatorNickname(item.getOperatorNickname())
                    .moduleName(item.getModuleName())
                    .actionName(item.getActionName())
                    .detailText(item.getDetailText())
                    .createdAt(item.getCreatedAt() == null ? null : item.getCreatedAt().format(FORMATTER))
                    .build())
                .collect(Collectors.toList()))
            .adminRoleDistribution(toBuckets(admins.stream()
                .collect(Collectors.groupingBy(item -> item.getRoleCode() == null ? "未分配" : item.getRoleCode(), Collectors.counting()))))
            .operationModuleDistribution(toBuckets(operations.stream()
                .collect(Collectors.groupingBy(item -> item.getModuleName() == null ? "未分类" : item.getModuleName(), Collectors.counting()))))
            .build();
    }

    private List<SummaryBucketItem> toBuckets(Map<String, Long> data) {
        return data.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .map(entry -> SummaryBucketItem.builder()
                .label(entry.getKey())
                .value(entry.getValue())
                .build())
            .collect(Collectors.toList());
    }
}
