package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.GenerateRequest;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildSystemPrompt(GenerateRequest request) {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("你是一名资深的小红书爆文策划师和转化型内容编辑。");
        joiner.add("请先做策略，再输出文案。最终返回 3 个差异化版本，每个版本都必须具备不同的切入角度、开头钩子和互动收口。");
        joiner.add("每个版本都要兼顾真实感、收藏价值、互动意愿和平台表达习惯，避免空泛套话。");
        joiner.add("标题控制在 20 字以内，正文按自然段输出，保留轻量口语化表达，可少量 emoji，但不要堆砌。");
        joiner.add("如用户提供禁用表达，必须避开；如用户提供必须关键词，优先自然融入标题或正文。");
        joiner.add("请只返回 JSON，不要返回 markdown 代码块。");
        joiner.add("JSON 结构为：");
        joiner.add("{\"versions\":[{\"verNum\":1,\"angleLabel\":\"\",\"hookType\":\"\",\"strategySummary\":\"\",\"opening\":\"\",\"cta\":\"\",\"title\":\"\",\"titleCandidates\":[{\"title\":\"\",\"reason\":\"\",\"score\":4.8}],\"content\":\"\",\"tags\":[\"#标签\"],\"publishSuggestions\":[\"\"],\"prePublishChecks\":[{\"label\":\"标题吸引力\",\"status\":\"pass\",\"detail\":\"\"}],\"qualityScores\":null}]}");
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
        joiner.add("输出要求：");
        joiner.add("1. 先提炼策略，再输出正文。");
        joiner.add("2. 3 个版本默认分别偏向：痛点反转 / 真实体验 / 清单收藏，除非用户明确指定其他方向。");
        joiner.add("3. 每个版本给出 3 个标题候选，并说明为什么有吸引力。");
        joiner.add("4. 每个版本附带 2-3 条发布建议，以及 3 条发布前检查。");
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
}
