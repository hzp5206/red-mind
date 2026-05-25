package com.redmind.modules.trending.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redmind.modules.trending.entity.TrendingCopyTask;
import com.redmind.modules.trending.mapper.TrendingCopyTaskMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

@Component
public class TrendingTaskScheduler {

    private final TrendingCopyTaskMapper trendingCopyTaskMapper;
    private final TrendingFetchService trendingFetchService;

    public TrendingTaskScheduler(TrendingCopyTaskMapper trendingCopyTaskMapper,
                                 TrendingFetchService trendingFetchService) {
        this.trendingCopyTaskMapper = trendingCopyTaskMapper;
        this.trendingFetchService = trendingFetchService;
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void scanAndRun() {
        List<TrendingCopyTask> tasks = trendingCopyTaskMapper.selectList(new LambdaQueryWrapper<TrendingCopyTask>()
            .eq(TrendingCopyTask::getEnabled, true));
        LocalDateTime now = LocalDateTime.now();
        for (TrendingCopyTask task : tasks) {
            if (shouldRun(task, now)) {
                try {
                    trendingFetchService.runTask(task.getId());
                } catch (Exception exception) {
                    trendingFetchService.markFailure(task, exception);
                }
            }
        }
    }

    private boolean shouldRun(TrendingCopyTask task, LocalDateTime now) {
        try {
            CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(task.getCronExpr());
            Date baseDate = Date.from((task.getLastRunAt() == null ? now.minusDays(1) : task.getLastRunAt())
                .atZone(ZoneId.systemDefault())
                .toInstant());
            Date nextDate = cronSequenceGenerator.next(baseDate);
            return nextDate != null && !nextDate.after(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
        } catch (Exception exception) {
            if (task.getLastRunAt() == null) {
                return true;
            }
            return task.getLastRunAt().isBefore(now.minusHours(12));
        }
    }
}
