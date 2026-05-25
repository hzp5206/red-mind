package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.TrendingReferenceCue;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildSystemPrompt(GenerateRequest request) {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("你是一名资深的小红书爆文策划师和转化型内容编辑。");
        joiner.add("请先做策略，再输出文案。最终返回 3 个差异化版本，每个版本都必须具备不同的切入角度、开头钩子和互动收口。");
        joiner.add("每个版本都要兼顾真实感、收藏价值、互动意愿和平台表达习惯，避免空泛套话。");
        joiner.add("标题控制在 20 字以内，正文按自然段输出，可以少量口语化，但不要堆砌。");
        joiner.add("如果用户提供了禁用表达，必须避开；如果用户提供了必须关键词，优先自然融入标题或正文。");
        joiner.add("如果用户附带了近期高热样本，请借鉴它们的策略优点，但不能复刻原句，必须输出差异化建议。");
        joiner.add("请只返回 JSON，不要返回 markdown 代码块。");
        joiner.add("JSON 结构如下：");
        joiner.add("{\"versions\":[{\"verNum\":1,\"angleLabel\":\"\",\"hookType\":\"\",\"strategySummary\":\"\",\"opening\":\"\",\"cta\":\"\",\"title\":\"\",\"titleCandidates\":[{\"title\":\"\",\"reason\":\"\",\"score\":4.8}],\"content\":\"\",\"tags\":[\"#标签\"],\"trendingReferenceTitles\":[\"\"],\"referenceTakeaways\":[\"\"],\"differentiationTips\":[\"\"],\"publishSuggestions\":[\"\"],\"prePublishChecks\":[{\"label\":\"标题吸引力\",\"status\":\"pass\",\"detail\":\"\"}],\"qualityScores\":null}]}");
        return joiner.toString();
    }

    public String buildUserPrompt(GenerateRequest request) {
        StringJoiner joiner = new StringJoiner("\n");
        add(joiner, "创作模式", request.getMode());
        add(joiner, "产品/主题", request.getProductName());
        add(joiner, "核心描述", request.getCoreDescription());
        add(joiner, "笔记风格", request.getStyle());
        add(joiner, "语气人设", request.getTone());
        add(joiner, "目标字数", request.getWordCount() == null ? null : String.valueOf(request.getWordCount()));
        add(joiner, "转化目标", request.getConversionGoal());
        add(joiner, "内容目标", request.getContentGoal());
        add(joiner, "钩子偏好", request.getHookPreference());
        add(joiner, "结构偏好", request.getNoteStructure());
        addList(joiner, "目标人群", request.getTargetAudience());
        addList(joiner, "核心卖点", request.getCoreSellingPoints());
        addList(joiner, "使用场景", request.getUseScenarios());
        addList(joiner, "必须关键词", request.getRequiredKeywords());
        addList(joiner, "禁用表达", request.getForbiddenExpressions());
        add(joiner, "参考链接", request.getReferenceUrl());
        add(joiner, "仿写样本", request.getStyleSample());
        add(joiner, "爆文参考策略", request.getTrendingStrategyBrief());
        addReferences(joiner, request.getTrendingReferences());
        joiner.add("输出要求：");
        joiner.add("1. 先提炼策略，再输出正文。");
        joiner.add("2. 3 个版本默认分别偏向：痛点反转 / 真实体验 / 清单收藏。");
        joiner.add("3. 每个版本给出 3 个标题候选，并说明为什么有吸引力。");
        joiner.add("4. 每个版本附带 2-3 条发布建议、3 条发布前检查、2-3 条样本借鉴点、2-3 条差异化建议。");
        joiner.add("5. 结尾必须有明确互动设计，例如评论引导、收藏提醒或私信引导。");
        return joiner.toString();
    }

    private void add(StringJoiner joiner, String label, String value) {
        if (StringUtils.isNotBlank(value)) {
            joiner.add(label + "：" + value);
        }
    }

    private void addList(StringJoiner joiner, String label, List<String> values) {
        if (values != null && !values.isEmpty()) {
            joiner.add(label + "：" + String.join("、", values));
        }
    }

    private void addReferences(StringJoiner joiner, List<TrendingReferenceCue> references) {
        if (references == null || references.isEmpty()) {
            return;
        }
        joiner.add("近期高热样本：");
        for (TrendingReferenceCue reference : references) {
            joiner.add("- 标题：" + reference.getTitle());
            joiner.add("  钩子：" + StringUtils.defaultIfBlank(reference.getHookType(), "未标注"));
            joiner.add("  结构：" + StringUtils.defaultIfBlank(reference.getStructureSummary(), "未标注"));
            joiner.add("  语气：" + StringUtils.defaultIfBlank(reference.getTone(), "未标注"));
            if (reference.getKeywords() != null && !reference.getKeywords().isEmpty()) {
                joiner.add("  关键词：" + reference.getKeywords().stream().collect(Collectors.joining("、")));
            }
            if (reference.getCollectPoints() != null && !reference.getCollectPoints().isEmpty()) {
                joiner.add("  可借鉴点：" + reference.getCollectPoints().stream().collect(Collectors.joining("；")));
            }
        }
    }
}
