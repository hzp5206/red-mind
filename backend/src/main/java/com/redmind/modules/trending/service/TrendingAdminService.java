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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private final ObjectMapper objectMapper;

    public TrendingAdminService(TrendingCopyTaskMapper trendingCopyTaskMapper,
                                TrendingCopyItemMapper trendingCopyItemMapper,
                                TrendingFetchService trendingFetchService,
                                OperationLogService operationLogService,
                                InspirationMapper inspirationMapper,
                                ObjectMapper objectMapper) {
        this.trendingCopyTaskMapper = trendingCopyTaskMapper;
        this.trendingCopyItemMapper = trendingCopyItemMapper;
        this.trendingFetchService = trendingFetchService;
        this.operationLogService = operationLogService;
        this.inspirationMapper = inspirationMapper;
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
        TrendingAnalysisResponse response = buildAnalysis(item);
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

    private TrendingAnalysisResponse buildAnalysis(TrendingCopyItem item) {
        String title = StringUtils.defaultString(item.getTitle());
        String content = StringUtils.defaultString(item.getContentText());
        List<String> tags = parseTags(item.getTagsJson());
        List<String> keywords = extractKeywords(item, tags);
        String titleType = detectTitleType(title);
        String hookType = detectHookType(title, content);
        String structureSummary = detectStructure(content);
        String interactionCta = detectInteractionCta(content);
        String tone = detectTone(title, content);
        List<String> collectPoints = buildCollectPoints(item, titleType, structureSummary);
        String recommendedStyle = recommendStyle(titleType, hookType, tags);
        String recommendedTone = recommendTone(tone);
        String recommendedHook = recommendHook(hookType);
        String recommendedStructure = recommendStructure(structureSummary);
        String productHint = StringUtils.defaultIfBlank(item.getKeyword(), inferProductHint(title, keywords));
        String coreDescription = buildCoreDescription(item, structureSummary, collectPoints, keywords);
        String styleSample = StringUtils.abbreviate(content, 280);
        String summary = buildSummary(titleType, hookType, tone);
        List<String> adaptationTips = buildAdaptationTips(item, collectPoints, recommendedStyle);

        return TrendingAnalysisResponse.builder()
            .itemId(item.getId())
            .titleType(titleType)
            .hookType(hookType)
            .structureSummary(structureSummary)
            .interactionCta(interactionCta)
            .collectPoints(collectPoints)
            .keywords(keywords)
            .tone(tone)
            .recommendedStyle(recommendedStyle)
            .recommendedTone(recommendedTone)
            .recommendedHook(recommendedHook)
            .recommendedStructure(recommendedStructure)
            .productHint(productHint)
            .coreDescription(coreDescription)
            .styleSample(styleSample)
            .requiredKeywords(keywords)
            .summary(summary)
            .adaptationTips(adaptationTips)
            .build();
    }

    private List<String> extractKeywords(TrendingCopyItem item, List<String> tags) {
        Set<String> keywords = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(item.getKeyword())) {
            keywords.add(item.getKeyword().trim());
        }
        for (String tag : tags) {
            String cleaned = StringUtils.removeStart(StringUtils.trimToEmpty(tag), "#");
            if (StringUtils.isNotBlank(cleaned)) {
                keywords.add(cleaned);
            }
        }
        if (keywords.isEmpty() && StringUtils.isNotBlank(item.getTitle())) {
            keywords.add(StringUtils.abbreviate(item.getTitle(), 12));
        }
        return new ArrayList<>(keywords);
    }

    private String detectTitleType(String title) {
        if (StringUtils.contains(title, "为什么") || StringUtils.contains(title, "为何")) {
            return "问题拆解型";
        }
        if (StringUtils.contains(title, "清单") || StringUtils.contains(title, "合集")) {
            return "清单合集型";
        }
        if (StringUtils.contains(title, "测评") || StringUtils.contains(title, "对比")) {
            return "测评对比型";
        }
        if (StringUtils.contains(title, "教程") || StringUtils.contains(title, "步骤")) {
            return "教程方法型";
        }
        return "经验种草型";
    }

    private String detectHookType(String title, String content) {
        if (StringUtils.contains(title, "为什么") || StringUtils.contains(content, "原来")) {
            return "问题切入";
        }
        if (StringUtils.contains(title, "别") || StringUtils.contains(title, "不要")) {
            return "避坑反差";
        }
        if (StringUtils.contains(title, "亲测") || StringUtils.contains(content, "我")) {
            return "第一人称体验";
        }
        if (StringUtils.contains(title, "清单") || StringUtils.contains(title, "合集")) {
            return "清单利益点";
        }
        return "结论先行";
    }

    private String detectStructure(String content) {
        String normalized = content.replace("\r", "\n");
        int paragraphCount = normalized.split("\\n+").length;
        if (StringUtils.contains(normalized, "1.") || StringUtils.contains(normalized, "①")
            || StringUtils.contains(normalized, "第一")) {
            return "清单分点结构";
        }
        if (StringUtils.contains(normalized, "问题") && StringUtils.contains(normalized, "建议")) {
            return "问题-方案结构";
        }
        if (paragraphCount >= 4) {
            return "场景递进结构";
        }
        return "短文种草结构";
    }

    private String detectInteractionCta(String content) {
        if (StringUtils.containsAny(content, "评论", "留言")) {
            return "评论互动";
        }
        if (StringUtils.containsAny(content, "收藏", "码住")) {
            return "收藏引导";
        }
        if (StringUtils.containsAny(content, "私信", "戳我")) {
            return "私信转化";
        }
        return "结尾补一句使用建议，并引导收藏";
    }

    private String detectTone(String title, String content) {
        String merged = (title + " " + content).toLowerCase(Locale.ROOT);
        if (StringUtils.containsAny(merged, "测评", "成分", "理性", "分析")) {
            return "专业理性";
        }
        if (StringUtils.containsAny(merged, "治愈", "温柔", "舒服")) {
            return "温柔治愈";
        }
        if (StringUtils.containsAny(merged, "笑死", "绝了", "太会了")) {
            return "轻松活泼";
        }
        return "真诚种草";
    }

    private List<String> buildCollectPoints(TrendingCopyItem item, String titleType, String structureSummary) {
        List<String> points = new ArrayList<>();
        points.add("标题带有明确结果感，适合拿来做开场钩子");
        if (item.getCollectsCount() != null && item.getCollectsCount() > 3000) {
            points.add("收藏量高，说明内容具备可复用和保存价值");
        }
        if (item.getCommentsCount() != null && item.getCommentsCount() > 800) {
            points.add("评论区活跃，说明话题具备讨论性");
        }
        if ("清单分点结构".equals(structureSummary) || "清单合集型".equals(titleType)) {
            points.add("结构清晰，适合迁移到清单式文案模板");
        }
        return points;
    }

    private String recommendStyle(String titleType, String hookType, List<String> tags) {
        if ("教程方法型".equals(titleType)) {
            return "tutorial";
        }
        if ("清单合集型".equals(titleType)) {
            return "collection";
        }
        if ("测评对比型".equals(titleType)) {
            return "ingredient";
        }
        if (tags.stream().anyMatch(tag -> StringUtils.contains(tag, "探店"))) {
            return "visit";
        }
        if ("第一人称体验".equals(hookType)) {
            return "vlog";
        }
        return "good_item";
    }

    private String recommendTone(String tone) {
        if ("专业理性".equals(tone)) {
            return "专业严谨";
        }
        if ("温柔治愈".equals(tone)) {
            return "温柔治愈";
        }
        if ("轻松活泼".equals(tone)) {
            return "幽默风趣";
        }
        return "真诚种草";
    }

    private String recommendHook(String hookType) {
        if ("问题切入".equals(hookType)) {
            return "问题切入";
        }
        if ("避坑反差".equals(hookType)) {
            return "反差开场";
        }
        if ("清单利益点".equals(hookType)) {
            return "清单利益点";
        }
        if ("第一人称体验".equals(hookType)) {
            return "第一人称体验";
        }
        return "结论先行";
    }

    private String recommendStructure(String structureSummary) {
        if ("问题-方案结构".equals(structureSummary)) {
            return "问题 → 方案 → CTA";
        }
        if ("清单分点结构".equals(structureSummary)) {
            return "清单 → 场景 → 建议";
        }
        if ("场景递进结构".equals(structureSummary)) {
            return "痛点 → 体验 → 结论";
        }
        return "对比 → 选择 → 互动";
    }

    private String inferProductHint(String title, List<String> keywords) {
        if (!keywords.isEmpty()) {
            return keywords.get(0);
        }
        return StringUtils.abbreviate(title, 12);
    }

    private String buildCoreDescription(TrendingCopyItem item, String structureSummary, List<String> collectPoints, List<String> keywords) {
        StringBuilder builder = new StringBuilder();
        builder.append("请参考这条爆文样本的表达方式，围绕“")
            .append(StringUtils.defaultIfBlank(item.getKeyword(), item.getTitle()))
            .append("”输出一篇更适合当前业务的内容。");
        builder.append("这条样本更像“").append(structureSummary).append("”，");
        if (!collectPoints.isEmpty()) {
            builder.append("值得借鉴的点包括：").append(String.join("；", collectPoints)).append("。");
        }
        if (!keywords.isEmpty()) {
            builder.append("可优先保留关键词：").append(String.join("、", keywords)).append("。");
        }
        return builder.toString();
    }

    private String buildSummary(String titleType, String hookType, String tone) {
        return "这条样本属于" + titleType + "，开场偏" + hookType + "，整体语气偏" + tone + "，适合改写成更强转化导向的发布文案。";
    }

    private List<String> buildAdaptationTips(TrendingCopyItem item, List<String> collectPoints, String recommendedStyle) {
        List<String> tips = new ArrayList<>();
        tips.add("保留样本中的高点击开场，但把表述替换成你自己的真实场景。");
        tips.add("正文不要照搬原句，改成“痛点-体验-结果”或“清单-建议”结构。");
        if (!collectPoints.isEmpty()) {
            tips.add("优先继承这个可收藏点：" + collectPoints.get(0));
        }
        tips.add("当前建议先用 " + recommendedStyle + " 风格生成，再做人手精修。");
        if (item.getNoteUrl() != null) {
            tips.add("发布前再核对一次来源内容，避免与原样本过度相似。");
        }
        return tips;
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
