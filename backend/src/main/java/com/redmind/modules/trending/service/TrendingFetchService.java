package com.redmind.modules.trending.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmind.modules.admin.service.OperationLogService;
import com.redmind.modules.trending.entity.TrendingCopyItem;
import com.redmind.modules.trending.entity.TrendingCopyTask;
import com.redmind.modules.trending.mapper.TrendingCopyItemMapper;
import com.redmind.modules.trending.mapper.TrendingCopyTaskMapper;
import com.redmind.modules.trending.provider.TrendingProviderRouter;
import com.redmind.modules.trending.provider.TrendingSourceItem;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TrendingFetchService {

    private final TrendingProviderRouter providerRouter;
    private final TrendingCopyTaskMapper trendingCopyTaskMapper;
    private final TrendingCopyItemMapper trendingCopyItemMapper;
    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    public TrendingFetchService(TrendingProviderRouter providerRouter,
                                TrendingCopyTaskMapper trendingCopyTaskMapper,
                                TrendingCopyItemMapper trendingCopyItemMapper,
                                OperationLogService operationLogService,
                                ObjectMapper objectMapper) {
        this.providerRouter = providerRouter;
        this.trendingCopyTaskMapper = trendingCopyTaskMapper;
        this.trendingCopyItemMapper = trendingCopyItemMapper;
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
    }

    public int runTask(Long taskId) {
        TrendingCopyTask task = trendingCopyTaskMapper.selectById(taskId);
        if (task == null) {
            return 0;
        }
        List<TrendingSourceItem> sourceItems = providerRouter.route(task.getProviderCode()).fetch(task);
        int inserted = 0;
        for (TrendingSourceItem sourceItem : sourceItems) {
            Long existed = trendingCopyItemMapper.selectCount(new LambdaQueryWrapper<TrendingCopyItem>()
                .eq(TrendingCopyItem::getTaskId, taskId)
                .eq(TrendingCopyItem::getSourceId, sourceItem.getSourceId()));
            if (existed != null && existed > 0) {
                continue;
            }
            TrendingCopyItem item = new TrendingCopyItem();
            item.setTaskId(taskId);
            item.setPlatformCode(task.getPlatformCode());
            item.setSourceId(sourceItem.getSourceId());
            item.setKeyword(sourceItem.getKeyword());
            item.setTitle(sourceItem.getTitle());
            item.setContentText(sourceItem.getContentText());
            item.setAuthorName(sourceItem.getAuthorName());
            item.setNoteUrl(sourceItem.getNoteUrl());
            item.setLikesCount(sourceItem.getLikesCount());
            item.setCollectsCount(sourceItem.getCollectsCount());
            item.setCommentsCount(sourceItem.getCommentsCount());
            item.setHeatScore(sourceItem.getHeatScore());
            try {
                item.setTagsJson(objectMapper.writeValueAsString(sourceItem.getTags()));
                item.setSnapshotJson(objectMapper.writeValueAsString(sourceItem));
            } catch (Exception exception) {
                item.setTagsJson("[]");
                item.setSnapshotJson("{}");
            }
            item.setPublishedAt(sourceItem.getPublishedAt());
            item.setFetchedAt(LocalDateTime.now());
            trendingCopyItemMapper.insert(item);
            inserted++;
        }

        task.setLastRunAt(LocalDateTime.now());
        task.setLastFetchedCount(inserted);
        task.setLastStatus("success");
        task.setLastMessage("本次新增 " + inserted + " 条爆文样本");
        task.setUpdatedAt(LocalDateTime.now());
        trendingCopyTaskMapper.updateById(task);
        operationLogService.log("trending_copy", "fetch", "trending_task", taskId, "执行爆文采集任务：" + task.getTaskName());
        return inserted;
    }

    public void markFailure(TrendingCopyTask task, Exception exception) {
        task.setLastRunAt(LocalDateTime.now());
        task.setLastFetchedCount(0);
        task.setLastStatus("failed");
        task.setLastMessage(exception.getMessage());
        task.setUpdatedAt(LocalDateTime.now());
        trendingCopyTaskMapper.updateById(task);
    }
}
