package com.redmind.modules.trending.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmind.modules.trending.dto.TrendingAnalysisResponse;
import com.redmind.modules.trending.entity.TrendingCopyItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TrendingAnalysisEngine {

    private final ObjectMapper objectMapper;

    public TrendingAnalysisEngine(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TrendingAnalysisResponse analyze(TrendingCopyItem item) {
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

    public List<String> parseTags(String tagsJson) {
        if (StringUtils.isBlank(tagsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (Exception exception) {
            return Collections.emptyList();
        }
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
}
