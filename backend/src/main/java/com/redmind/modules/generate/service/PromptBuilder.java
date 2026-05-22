package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.GenerateRequest;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildSystemPrompt(GenerateRequest request) {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("你是一名资深小红书爆文策略师。");
        joiner.add("请根据用户输入生成 3 个版本的标题、正文和话题标签。");
        joiner.add("风格：" + request.getStyle());
        joiner.add("语气：" + request.getTone());
        joiner.add("目标字数：" + request.getWordCount());
        if (request.getTargetAudience() != null && !request.getTargetAudience().isEmpty()) {
            joiner.add("目标人群：" + String.join("、", request.getTargetAudience()));
        }
        if (request.getRequiredKeywords() != null && !request.getRequiredKeywords().isEmpty()) {
            joiner.add("必须包含关键词：" + String.join("、", request.getRequiredKeywords()));
        }
        joiner.add("输出要求：标题 20 字内，正文分段，适当 emoji，标签 5-10 个。");
        joiner.add("请只返回 JSON，不要返回 markdown 代码块。");
        joiner.add("JSON 结构为：{\"versions\":[{\"verNum\":1,\"title\":\"\",\"content\":\"\",\"tags\":[\"#标签\"],\"qualityScores\":null}]}。");
        return joiner.toString();
    }

    public String buildUserPrompt(GenerateRequest request) {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("创作模式：" + request.getMode());
        joiner.add("核心描述：" + request.getCoreDescription());
        joiner.add("笔记风格：" + request.getStyle());
        joiner.add("语气人设：" + request.getTone());
        joiner.add("字数要求：" + request.getWordCount());
        if (request.getTargetAudience() != null && !request.getTargetAudience().isEmpty()) {
            joiner.add("目标人群：" + String.join("、", request.getTargetAudience()));
        }
        if (request.getRequiredKeywords() != null && !request.getRequiredKeywords().isEmpty()) {
            joiner.add("必须包含：" + String.join("、", request.getRequiredKeywords()));
        }
        if (request.getReferenceUrl() != null) {
            joiner.add("参考链接：" + request.getReferenceUrl());
        }
        if (request.getStyleSample() != null) {
            joiner.add("仿写样本：" + request.getStyleSample());
        }
        joiner.add("请输出 3 个差异化版本，开头钩子、结构和结尾 CTA 不同。");
        return joiner.toString();
    }
}
