package com.redmind.modules.trending.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmind.common.exception.BizException;
import com.redmind.common.security.JwtUserContext;
import com.redmind.modules.admin.service.OperationLogService;
import com.redmind.modules.common.dto.PageResponse;
import com.redmind.modules.generate.entity.Inspiration;
import com.redmind.modules.generate.mapper.InspirationMapper;
import com.redmind.modules.trending.dto.TrendingAnalysisResponse;
import com.redmind.modules.trending.dto.TrendingDashboardResponse;
import com.redmind.modules.trending.dto.TrendingItemResponse;
import com.redmind.modules.trending.dto.TrendingTaskResponse;
import com.redmind.modules.trending.dto.TrendingTaskSaveRequest;
import com.redmind.modules.trending.entity.TrendingCopyItem;
import com.redmind.modules.trending.entity.TrendingCopyTask;
import com.redmind.modules.trending.mapper.TrendingCopyItemMapper;
import com.redmind.modules.trending.mapper.TrendingCopyTaskMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TrendingAdminService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TrendingCopyTaskMapper trendingCopyTaskMapper;
    private final TrendingCopyItemMapper trendingCopyItemMapper;
    private final TrendingFetchService trendingFetchService;
    private final OperationLogService operationLogService;
    private final InspirationMapper inspirationMapper;
    private final TrendingAnalysisEngine trendingAnalysisEngine;
    private final ObjectMapper objectMapper;

    public TrendingAdminService(TrendingCopyTaskMapper trendingCopyTaskMapper,
                                TrendingCopyItemMapper trendingCopyItemMapper,
                                TrendingFetchService trendingFetchService,
                                OperationLogService operationLogService,
                                InspirationMapper inspirationMapper,
                                TrendingAnalysisEngine trendingAnalysisEngine,
                                ObjectMapper objectMapper) {
        this.trendingCopyTaskMapper = trendingCopyTaskMapper;
        this.trendingCopyItemMapper = trendingCopyItemMapper;
        this.trendingFetchService = trendingFetchService;
        this.operationLogService = operationLogService;
        this.inspirationMapper = inspirationMapper;
        this.trendingAnalysisEngine = trendingAnalysisEngine;
        this.objectMapper = objectMapper;
    }

    public TrendingDashboardResponse dashboard() {
        List<TrendingCopyTask> tasks = trendingCopyTaskMapper.selectList(new LambdaQueryWrapper<TrendingCopyTask>()
            .orderByDesc(TrendingCopyTask::getId));
        List<TrendingCopyItem> latestItems = trendingCopyItemMapper.selectList(new LambdaQueryWrapper<TrendingCopyItem>()
            .orderByDesc(TrendingCopyItem::getFetchedAt)
            .last("limit 10"));
        Long todayFetchedCount = trendingCopyItemMapper.selectCount(new LambdaQueryWrapper<TrendingCopyItem>()
            .ge(TrendingCopyItem::getFetchedAt, LocalDate.now().atStartOfDay()));
        Long totalItemCount = trendingCopyItemMapper.selectCount(null);
        return TrendingDashboardResponse.builder()
            .taskCount((long) tasks.size())
            .enabledTaskCount(tasks.stream().filter(task -> Boolean.TRUE.equals(task.getEnabled())).count())
            .totalItemCount(totalItemCount == null ? 0L : totalItemCount)
            .todayFetchedCount(todayFetchedCount == null ? 0L : todayFetchedCount)
            .tasks(tasks.stream().map(this::toTaskResponse).collect(Collectors.toList()))
            .latestItems(latestItems.stream().map(this::toItemResponse).collect(Collectors.toList()))
            .build();
    }

    public PageResponse<TrendingItemResponse> pageItems(String keyword, String platformCode, Integer pageNum, Integer pageSize) {
        Page<TrendingCopyItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TrendingCopyItem> wrapper = new LambdaQueryWrapper<TrendingCopyItem>()
            .orderByDesc(TrendingCopyItem::getHeatScore)
            .orderByDesc(TrendingCopyItem::getFetchedAt);
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(query -> query.like(TrendingCopyItem::getTitle, keyword)
                .or()
                .like(TrendingCopyItem::getKeyword, keyword)
                .or()
                .like(TrendingCopyItem::getContentText, keyword));
        }
        if (StringUtils.isNotBlank(platformCode)) {
            wrapper.eq(TrendingCopyItem::getPlatformCode, platformCode);
        }
        Page<TrendingCopyItem> result = trendingCopyItemMapper.selectPage(page, wrapper);
        return new PageResponse<>(result.getTotal(), result.getRecords().stream().map(this::toItemResponse).collect(Collectors.toList()));
    }

    public List<TrendingTaskResponse> listTasks() {
        return trendingCopyTaskMapper.selectList(new LambdaQueryWrapper<TrendingCopyTask>().orderByDesc(TrendingCopyTask::getId))
            .stream()
            .map(this::toTaskResponse)
            .collect(Collectors.toList());
    }

    public TrendingTaskResponse saveTask(TrendingTaskSaveRequest request) {
        TrendingCopyTask task = request.getId() == null ? new TrendingCopyTask() : trendingCopyTaskMapper.selectById(request.getId());
        if (task == null) {
            throw new BizException("采集任务不存在");
        }
        task.setTaskName(request.getTaskName());
        task.setPlatformCode(request.getPlatformCode());
        task.setKeywords(request.getKeywords());
        task.setFetchLimit(request.getFetchLimit());
        task.setCronExpr(request.getCronExpr());
        task.setEnabled(request.getEnabled());
        task.setProviderCode(request.getProviderCode());
        task.setUpdatedAt(LocalDateTime.now());
        if (task.getId() == null) {
            task.setCreatedAt(LocalDateTime.now());
            task.setLastStatus("idle");
            task.setLastMessage("等待首次执行");
            trendingCopyTaskMapper.insert(task);
            operationLogService.log("trending_copy", "create", "trending_task", task.getId(), "新增爆文采集任务：" + task.getTaskName());
        } else {
            trendingCopyTaskMapper.updateById(task);
            operationLogService.log("trending_copy", "update", "trending_task", task.getId(), "更新爆文采集任务：" + task.getTaskName());
        }
        return toTaskResponse(task);
    }

    public void deleteTask(Long id) {
        TrendingCopyTask task = trendingCopyTaskMapper.selectById(id);
        trendingCopyTaskMapper.deleteById(id);
        operationLogService.log("trending_copy", "delete", "trending_task", id, "删除爆文采集任务：" + (task == null ? id : task.getTaskName()));
    }

    public int trigger(Long id) {
        return trendingFetchService.runTask(id);
    }

    public TrendingAnalysisResponse analyzeItem(Long id) {
        TrendingCopyItem item = trendingCopyItemMapper.selectById(id);
        if (item == null) {
            throw new BizException("爆文样本不存在");
        }
        if (StringUtils.isNotBlank(item.getAnalysisJson())) {
            try {
                return objectMapper.readValue(item.getAnalysisJson(), TrendingAnalysisResponse.class);
            } catch (Exception ignored) {
            }
        }
        TrendingAnalysisResponse response = trendingAnalysisEngine.analyze(item);
        try {
            item.setAnalysisJson(objectMapper.writeValueAsString(response));
            trendingCopyItemMapper.updateById(item);
        } catch (Exception ignored) {
        }
        return response;
    }

    public void collectItem(Long id) {
        Long userId = JwtUserContext.getUserId();
        if (userId == null) {
            throw new BizException("请先登录后再收藏");
        }
        TrendingCopyItem item = trendingCopyItemMapper.selectById(id);
        if (item == null) {
            throw new BizException("爆文样本不存在");
        }
        Long existed = inspirationMapper.selectCount(new LambdaQueryWrapper<Inspiration>()
            .eq(Inspiration::getUserId, userId)
            .eq(Inspiration::getTrendingItemId, id));
        if (existed != null && existed > 0) {
            return;
        }

        Inspiration inspiration = new Inspiration();
        inspiration.setUserId(userId);
        inspiration.setHistoryId(null);
        inspiration.setTrendingItemId(id);
        inspiration.setCustomTags("爆文采集");
        inspirationMapper.insert(inspiration);
        operationLogService.log("trending_copy", "collect", "trending_item", id, "收藏爆文样本：" + item.getTitle());
    }

    private TrendingTaskResponse toTaskResponse(TrendingCopyTask task) {
        return TrendingTaskResponse.builder()
            .id(task.getId())
            .taskName(task.getTaskName())
            .platformCode(task.getPlatformCode())
            .keywords(task.getKeywords())
            .fetchLimit(task.getFetchLimit())
            .cronExpr(task.getCronExpr())
            .providerCode(task.getProviderCode())
            .enabled(task.getEnabled())
            .lastRunAt(format(task.getLastRunAt()))
            .lastFetchedCount(task.getLastFetchedCount())
            .lastStatus(task.getLastStatus())
            .lastMessage(task.getLastMessage())
            .createdAt(format(task.getCreatedAt()))
            .updatedAt(format(task.getUpdatedAt()))
            .build();
    }

    private TrendingItemResponse toItemResponse(TrendingCopyItem item) {
        return TrendingItemResponse.builder()
            .id(item.getId())
            .taskId(item.getTaskId())
            .platformCode(item.getPlatformCode())
            .sourceId(item.getSourceId())
            .keyword(item.getKeyword())
            .title(item.getTitle())
            .contentText(item.getContentText())
            .authorName(item.getAuthorName())
            .noteUrl(item.getNoteUrl())
            .likesCount(item.getLikesCount())
            .collectsCount(item.getCollectsCount())
            .commentsCount(item.getCommentsCount())
            .heatScore(item.getHeatScore())
            .tags(parseTags(item.getTagsJson()))
            .publishedAt(format(item.getPublishedAt()))
            .fetchedAt(format(item.getFetchedAt()))
            .build();
    }

    private List<String> parseTags(String tagsJson) {
        if (StringUtils.isBlank(tagsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    private String format(LocalDateTime time) {
        return time == null ? null : time.format(FORMATTER);
    }
}
