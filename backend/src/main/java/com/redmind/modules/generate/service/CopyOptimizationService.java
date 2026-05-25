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
        if ("stronger_hook".equals(option)) {
            return strongerHook(request);
        }
        if ("more_emotional".equals(option)) {
            return moreEmotional(request);
        }
        if ("more_collectible".equals(option)) {
            return moreCollectible(request);
        }
        if ("more_natural".equals(option)) {
            return moreNatural(request);
        }
        if ("stronger_cta".equals(option)) {
            return strongerCta(request);
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
        List<String> icons = Arrays.asList("✨", "📌", "💡", "🧾", "🔥");
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
        String newOpening = "先说结论：这篇我会从真实使用感、适合人群和避坑点三个角度，帮你快速判断值不值得入。";
        if (!blocks.isEmpty()) {
            blocks.set(0, newOpening);
        } else {
            blocks.add(newOpening);
        }
        return new OptimizeResponse(request.getTitle(), String.join("\n\n", blocks), request.getTags());
    }

    private OptimizeResponse strongerHook(OptimizeRequest request) {
        List<String> blocks = splitParagraphs(request.getContent());
        String opening = "如果你也在犹豫要不要冲，先别急，我把最容易踩坑和最值得关注的点，直接一次讲明白。";
        if (blocks.isEmpty()) {
            blocks.add(opening);
        } else {
            blocks.set(0, opening);
        }
        String title = request.getTitle().contains("别") ? request.getTitle() : "先别急，" + request.getTitle();
        return new OptimizeResponse(title, String.join("\n\n", blocks), request.getTags());
    }

    private OptimizeResponse moreEmotional(OptimizeRequest request) {
        List<String> blocks = splitParagraphs(request.getContent());
        if (!blocks.isEmpty()) {
            blocks.add("说实话，我最怕那种看起来很惊艳、实际用起来却落差很大的内容，所以这次更想把真实感受说透。");
        }
        return new OptimizeResponse("真心话：" + request.getTitle(), String.join("\n\n", blocks), request.getTags());
    }

    private OptimizeResponse moreCollectible(OptimizeRequest request) {
        List<String> blocks = splitParagraphs(request.getContent());
        blocks.add("建议直接收藏这篇，下次如果要重新判断，就按这 3 个标准逐条对照：是否适合自己、是否真的解决痛点、是否值得长期使用。");
        List<String> tags = new ArrayList<>(request.getTags() == null ? new ArrayList<>() : request.getTags());
        if (!tags.contains("#建议先收藏")) {
            tags.add("#建议先收藏");
        }
        return new OptimizeResponse(request.getTitle(), String.join("\n\n", blocks), tags);
    }

    private OptimizeResponse moreNatural(OptimizeRequest request) {
        String content = StringUtils.defaultString(request.getContent())
            .replace("非常", "挺")
            .replace("强烈推荐", "会更愿意推荐")
            .replace("一定要", "可以优先考虑");
        String title = request.getTitle().replace("必看", "可以看看");
        return new OptimizeResponse(title, content, request.getTags());
    }

    private OptimizeResponse strongerCta(OptimizeRequest request) {
        List<String> blocks = splitParagraphs(request.getContent());
        String cta = "如果你更在意效果还是体验感，评论区告诉我，我可以继续按你的关注点帮你拆。";
        if (blocks.isEmpty()) {
            blocks.add(cta);
        } else {
            blocks.set(blocks.size() - 1, cta);
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
