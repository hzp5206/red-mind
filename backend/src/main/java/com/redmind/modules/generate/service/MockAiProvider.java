package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GeneratedVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockAiProvider implements AiProvider {

    @Override
    public boolean support(String providerCode) {
        return "mock".equalsIgnoreCase(providerCode);
    }

    @Override
    public List<GeneratedVersion> generate(GenerateRequest request) {
        List<GeneratedVersion> versions = new ArrayList<>();
        List<String> coreWords = request.getRequiredKeywords() == null || request.getRequiredKeywords().isEmpty()
            ? Arrays.asList("真实体验", "氛围感", "种草")
            : request.getRequiredKeywords();

        for (int i = 1; i <= 3; i++) {
            String title = buildTitle(request, i);
            String content = buildContent(request, i, coreWords);
            List<String> tags = Arrays.asList(
                "#小红书爆文",
                "#" + request.getStyle(),
                "#" + (request.getTargetAudience() == null || request.getTargetAudience().isEmpty()
                    ? "精准种草" : request.getTargetAudience().get(0)),
                "#内容营销",
                "#高转化文案"
            );
            versions.add(GeneratedVersion.builder()
                .verNum(i)
                .title(title)
                .content(content)
                .tags(tags)
                .build());
        }
        return versions;
    }

    private String buildTitle(GenerateRequest request, int index) {
        String[] hooks = {"别再盲买了", "真的会反复回购", "这篇给你讲透"};
        return "✨" + hooks[index - 1] + "｜" + trimTitle(request.getCoreDescription());
    }

    private String buildContent(GenerateRequest request, int index, List<String> words) {
        StringBuilder builder = new StringBuilder();
        builder.append("姐妹们，今天认真聊聊 ").append(request.getCoreDescription()).append("。\n\n");
        builder.append("我按 “痛点-体验-结论” 的结构整理了第 ").append(index).append(" 个版本，更适合 ")
            .append(request.getTargetAudience() == null || request.getTargetAudience().isEmpty()
                ? "泛人群种草"
                : String.join(" / ", request.getTargetAudience()))
            .append("。\n\n");
        builder.append("先说最戳我的点：").append(words.get(0)).append("、")
            .append(words.size() > 1 ? words.get(1) : "高级感")
            .append("、").append(words.size() > 2 ? words.get(2) : "转化力").append(" 都能自然融进去。\n\n");
        builder.append("如果你想要的是更像真实分享、而不是硬广堆砌，这版会更稳。最后再补一句互动钩子：你们更在意效果还是肤感？");
        return builder.toString();
    }

    private String trimTitle(String text) {
        return text.length() > 16 ? text.substring(0, 16) : text;
    }
}
