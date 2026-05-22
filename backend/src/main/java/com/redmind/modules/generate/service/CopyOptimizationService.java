package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.OptimizeRequest;
import com.redmind.modules.generate.dto.OptimizeResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CopyOptimizationService {

    public OptimizeResponse optimize(OptimizeRequest request) {
        String option = request.getOption() == null ? "" : request.getOption().toLowerCase();
        if ("concise".equals(option)) {
            return concise(request);
        }
        if ("rich".equals(option)) {
            return rich(request);
        }
        if ("emoji".equals(option)) {
            return emoji(request);
        }
        if ("rewrite_opening".equals(option)) {
            return rewriteOpening(request);
        }
        return new OptimizeResponse(request.getTitle(), request.getContent(), request.getTags());
    }

    private OptimizeResponse concise(OptimizeRequest request) {
        String normalized = request.getContent().replace("\r", "");
        String content = normalized.length() > 200 ? normalized.substring(0, 200) + "..." : normalized;
        String title = request.getTitle().length() > 18 ? request.getTitle().substring(0, 18) : request.getTitle();
        return new OptimizeResponse(title, content, request.getTags());
    }

    private OptimizeResponse rich(OptimizeRequest request) {
        List<String> blocks = splitParagraphs(request.getContent());
        blocks.add("如果你也在找更稳妥的表达方式，可以把自己的真实体验、使用场景和人群痛点再补一层，整体转化感会更自然。");
        blocks.add("最后记得留一个互动问题：你更关注效果、性价比，还是长期使用感受？");
        return new OptimizeResponse(request.getTitle(), String.join("\n\n", blocks), request.getTags());
    }

    private OptimizeResponse emoji(OptimizeRequest request) {
        List<String> icons = Arrays.asList("✨", "📌", "💡", "🫶", "🔥");
        List<String> blocks = splitParagraphs(request.getContent());
        List<String> result = new ArrayList<>();
        for (int index = 0; index < blocks.size(); index++) {
            String icon = icons.get(index % icons.size());
            result.add(icon + " " + blocks.get(index));
        }
        return new OptimizeResponse(request.getTitle(), String.join("\n\n", result), request.getTags());
    }

    private OptimizeResponse rewriteOpening(OptimizeRequest request) {
        List<String> blocks = splitParagraphs(request.getContent());
        String newOpening = "先说结论：这篇我会从真实使用感、适合人群和踩坑点三个角度，帮你快速判断值不值得入。";
        if (!blocks.isEmpty()) {
            blocks.set(0, newOpening);
        } else {
            blocks.add(newOpening);
        }
        return new OptimizeResponse(request.getTitle(), String.join("\n\n", blocks), request.getTags());
    }

    private List<String> splitParagraphs(String content) {
        String[] paragraphs = StringUtils.defaultString(content).replace("\r", "").split("\n\n");
        List<String> result = new ArrayList<>();
        for (String paragraph : paragraphs) {
            if (StringUtils.isNotBlank(paragraph)) {
                result.add(paragraph.trim());
            }
        }
        if (result.isEmpty() && StringUtils.isNotBlank(content)) {
            result.add(content.trim());
        }
        return result;
    }
}
