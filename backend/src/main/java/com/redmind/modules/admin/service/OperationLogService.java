package com.redmind.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.redmind.common.exception.BizException;
import com.redmind.common.security.JwtUserContext;
import com.redmind.modules.admin.dto.OperationLogQueryRequest;
import com.redmind.modules.admin.dto.OperationLogResponse;
import com.redmind.modules.admin.entity.OperationLog;
import com.redmind.modules.admin.mapper.OperationLogMapper;
import com.redmind.modules.common.dto.PageResponse;
import com.redmind.modules.user.entity.User;
import com.redmind.modules.user.mapper.UserMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OperationLogMapper operationLogMapper;
    private final UserMapper userMapper;

    public OperationLogService(OperationLogMapper operationLogMapper, UserMapper userMapper) {
        this.operationLogMapper = operationLogMapper;
        this.userMapper = userMapper;
    }

    public void log(String moduleName, String actionName, String targetType, Long targetId, String detailText) {
        Long userId = JwtUserContext.getUserId();
        if (userId == null) {
            return;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        OperationLog record = new OperationLog();
        record.setOperatorId(userId);
        record.setOperatorNickname(user.getNickname());
        record.setModuleName(moduleName);
        record.setActionName(actionName);
        record.setTargetType(targetType);
        record.setTargetId(targetId);
        record.setDetailText(detailText);
        record.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(record);
    }

    public PageResponse<OperationLogResponse> page(OperationLogQueryRequest request, Integer pageNum, Integer pageSize) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getId);
        if (request != null) {
            if (request.getOperatorId() != null) {
                wrapper.eq(OperationLog::getOperatorId, request.getOperatorId());
            }
            if (StringUtils.isNotBlank(request.getModuleName())) {
                wrapper.eq(OperationLog::getModuleName, request.getModuleName());
            }
            if (StringUtils.isNotBlank(request.getActionName())) {
                wrapper.eq(OperationLog::getActionName, request.getActionName());
            }
            if (request.getStartDate() != null) {
                wrapper.ge(OperationLog::getCreatedAt, request.getStartDate().atStartOfDay());
            }
            if (request.getEndDate() != null) {
                wrapper.le(OperationLog::getCreatedAt, LocalDateTime.of(request.getEndDate(), LocalTime.MAX));
            }
        }
        Page<OperationLog> result = operationLogMapper.selectPage(page, wrapper);
        return new PageResponse<>(
            result.getTotal(),
            result.getRecords().stream().map(item -> OperationLogResponse.builder()
                .id(item.getId())
                .operatorId(item.getOperatorId())
                .operatorNickname(item.getOperatorNickname())
                .moduleName(item.getModuleName())
                .actionName(item.getActionName())
                .targetType(item.getTargetType())
                .targetId(item.getTargetId())
                .detailText(item.getDetailText())
                .createdAt(item.getCreatedAt() == null ? null : item.getCreatedAt().format(FORMATTER))
                .build())
                .collect(Collectors.toList())
        );
    }

    public OperationLog requireOne(Long id) {
        OperationLog log = operationLogMapper.selectById(id);
        if (log == null) {
            throw new BizException("操作日志不存在");
        }
        return log;
    }
}
