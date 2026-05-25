package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GeneratedVersion;
import com.redmind.modules.generate.dto.PrePublishCheckItem;
import com.redmind.modules.generate.dto.TitleCandidate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MockAiProvider implements AiProvider {

    private static final List<VersionBlueprint> BLUEPRINTS = Arrays.asList(
        new VersionBlueprint("痛点反转型", "反差开场", "评论区告诉我你最容易踩的坑，我继续帮你拆"),
        new VersionBlueprint("真实体验型", "第一人称体验", "如果你也在犹豫，留言“想看”我把完整体验补上"),
        new VersionBlueprint("清单收藏型", "清单利益点", "先收藏再慢慢对照，省得下次找不到")
    );

    @Override
    public boolean support(String providerCode) {
        return "mock".equalsIgnoreCase(providerCode);
    }

    @Override
    public List<GeneratedVersion> generate(GenerateRequest request) {
        List<GeneratedVersion> versions = new ArrayList<>();
        List<String> coreWords = fallbackList(
            request.getRequiredKeywords(),
            Arrays.asList(normalizeProductName(request), "真实体验", "种草建议")
        );
        List<String> sellingPoints = fallbackList(
            request.getCoreSellingPoints(),
            Arrays.asList("解决核心痛点", "表达真实使用感", "更容易形成收藏动作")
        );
        List<String> scenarios = fallbackList(
            request.getUseScenarios(),
            Arrays.asList("通勤前", "出门前", "日常复购场景")
        );
        List<String> audiences = fallbackList(request.getTargetAudience(), Arrays.asList("精准种草人群"));

        for (int index = 0; index < BLUEPRINTS.size(); index++) {
            VersionBlueprint blueprint = BLUEPRINTS.get(index);
            List<TitleCandidate> titleCandidates = buildTitleCandidates(request, blueprint, coreWords, sellingPoints, index);
            String title = titleCandidates.get(0).getTitle();
            String opening = buildOpening(request, scenarios, index);
            String cta = buildCta(request, blueprint);
            String content = buildContent(request, opening, cta, coreWords, sellingPoints, scenarios, audiences);
            versions.add(GeneratedVersion.builder()
                .verNum(index + 1)
                .angleLabel(blueprint.getAngleLabel())
                .hookType(blueprint.getHookType())
                .strategySummary(buildStrategySummary(request, blueprint, sellingPoints, scenarios, audiences))
                .opening(opening)
                .cta(cta)
                .title(title)
                .titleCandidates(titleCandidates)
                .content(content)
                .tags(buildTags(request, audiences, coreWords))
                .publishSuggestions(buildPublishSuggestions(request, blueprint))
                .prePublishChecks(buildPrePublishChecks(title, content, request))
                .build());
        }
        return versions;
    }

    private List<TitleCandidate> buildTitleCandidates(GenerateRequest request,
                                                      VersionBlueprint blueprint,
                                                      List<String> keywords,
                                                      List<String> sellingPoints,
                                                      int index) {
        String subject = normalizeProductName(request);
        List<String> titles = new ArrayList<>();
        if (index == 0) {
            titles.add("别再盲冲了，" + trimTitle(subject) + "这样选更稳");
            titles.add(trimTitle(subject) + "避坑点，我真的后悔没早知道");
            titles.add("想把" + trimTitle(subject) + "买对，先看这 3 点");
        } else if (index == 1) {
            titles.add("我把" + trimTitle(subject) + "用了一阵，感受很真实");
            titles.add(trimTitle(subject) + "不是玄学，真实变化都在这");
            titles.add("真心话：这次" + trimTitle(subject) + "让我想回购");
        } else {
            titles.add(trimTitle(subject) + "值不值得冲？这篇帮你省时间");
            titles.add("关于" + trimTitle(subject) + "，先收藏这份决策清单");
            titles.add(trimTitle(subject) + "怎么买更稳？直接抄这份思路");
        }

        List<TitleCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            candidates.add(TitleCandidate.builder()
                .title(sanitize(titles.get(i), request.getForbiddenExpressions()))
                .reason(buildTitleReason(blueprint, keywords, sellingPoints, i))
                .score(Math.max(4.2D, 4.9D - (i * 0.2D)))
                .build());
        }
        return candidates;
    }

    private String buildOpening(GenerateRequest request, List<String> scenarios, int index) {
        String subject = normalizeProductName(request);
        if (index == 0) {
            return sanitize(
                "如果你也在" + scenarios.get(0) + "反复纠结 " + subject + "，先别急着下单，我先把最容易踩坑的地方讲透。",
                request.getForbiddenExpressions()
            );
        }
        if (index == 1) {
            return sanitize(
                "这次我不是想硬夸 " + subject + "，而是把自己真实用了之后的感受、变化和小遗憾都一次说清。",
                request.getForbiddenExpressions()
            );
        }
        return sanitize(
            "如果你想快速判断 " + subject + " 到底适不适合自己，这篇直接给你一份能收藏复看的决策清单。",
            request.getForbiddenExpressions()
        );
    }

    private String buildCta(GenerateRequest request, VersionBlueprint blueprint) {
        if (StringUtils.isNotBlank(request.getContentGoal()) && request.getContentGoal().contains("私信")) {
            return "想要我把这套话术整理成可直接套用的版本，评论区留“模板”或者直接私信我。";
        }
        if (StringUtils.isNotBlank(request.getConversionGoal()) && request.getConversionGoal().contains("下单")) {
            return "如果你已经心动了，先把这篇收藏起来，对照清楚再决定，别被情绪催着下单。";
        }
        return blueprint.getCta();
    }

    private String buildContent(GenerateRequest request,
                                String opening,
                                String cta,
                                List<String> words,
                                List<String> sellingPoints,
                                List<String> scenarios,
                                List<String> audiences) {
        StringBuilder builder = new StringBuilder();
        builder.append(opening).append("\n\n");
        builder.append("我把这版内容按“")
            .append(StringUtils.defaultIfBlank(request.getNoteStructure(), "痛点 → 体验 → 结论"))
            .append("”来展开，更适合 ")
            .append(String.join(" / ", audiences))
            .append(" 快速判断。")
            .append("\n\n");
        builder.append("先说最值得关注的 3 个点：")
            .append(sellingPoints.get(0))
            .append("、")
            .append(sellingPoints.size() > 1 ? sellingPoints.get(1) : words.get(0))
            .append("、")
            .append(sellingPoints.size() > 2 ? sellingPoints.get(2) : words.get(Math.min(1, words.size() - 1)))
            .append("。如果你最在意的是 ")
            .append(words.get(0))
            .append("，这篇会更有参考价值。")
            .append("\n\n");
        builder.append("我会重点结合 ")
            .append(scenarios.get(0))
            .append("、")
            .append(scenarios.size() > 1 ? scenarios.get(1) : "日常使用")
            .append(" 这两个场景来讲，尽量不说空话，直接告诉你哪些点真的有感，哪些点只是看起来很吸引人。")
            .append("\n\n");
        builder.append("最后给一个明确建议：如果你的目标是")
            .append(StringUtils.defaultIfBlank(request.getConversionGoal(), "种草转化"))
            .append("，那就优先围绕“")
            .append(sellingPoints.get(0))
            .append("”这个核心卖点去表达，会更容易让人记住并产生行动。")
            .append("\n\n")
            .append(cta);
        return builder.toString();
    }

    private String buildStrategySummary(GenerateRequest request,
                                        VersionBlueprint blueprint,
                                        List<String> sellingPoints,
                                        List<String> scenarios,
                                        List<String> audiences) {
        return sanitize(
            "围绕“" + sellingPoints.get(0) + "”切入，用" + blueprint.getHookType()
                + "抓住停留，再结合 " + String.join(" / ", scenarios)
                + " 的真实场景，把内容重心落在 " + String.join(" / ", audiences)
                + " 最关心的决策点上。",
            request.getForbiddenExpressions()
        );
    }

    private List<String> buildTags(GenerateRequest request, List<String> audiences, List<String> coreWords) {
        List<String> tags = new ArrayList<>();
        tags.add("#小红书文案");
        tags.add("#" + normalizeStyleTag(request.getStyle()));
        tags.add("#" + audiences.get(0));
        tags.add("#" + trimTitle(coreWords.get(0)));
        tags.add("#高质量种草");
        tags.add("#爆文选题");
        return tags;
    }

    private List<String> buildPublishSuggestions(GenerateRequest request, VersionBlueprint blueprint) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("封面首屏直接写出“" + blueprint.getAngleLabel() + "”结论，提升停留。");
        suggestions.add("前 2 段尽量保留第一人称表达，让内容更像真人分享。");
        if (StringUtils.isNotBlank(request.getContentGoal()) && request.getContentGoal().contains("评论")) {
            suggestions.add("结尾问题尽量二选一，能更自然拉起评论互动。");
        } else {
            suggestions.add("结尾加收藏提醒，比泛泛点赞引导更容易转化。");
        }
        return suggestions;
    }

    private List<PrePublishCheckItem> buildPrePublishChecks(String title, String content, GenerateRequest request) {
        List<PrePublishCheckItem> checks = new ArrayList<>();
        checks.add(PrePublishCheckItem.builder()
            .label("标题吸引力")
            .status(title.length() <= 20 ? "pass" : "warn")
            .detail(title.length() <= 20 ? "长度合适，首屏更聚焦" : "标题偏长，建议压缩到 20 字内")
            .build());
        checks.add(PrePublishCheckItem.builder()
            .label("收藏价值")
            .status(content.contains("3 个点") || content.contains("清单") ? "pass" : "warn")
            .detail(content.contains("3 个点") || content.contains("清单") ? "有明确的信息结构，适合收藏" : "建议加入步骤、清单或判断标准")
            .build());
        checks.add(PrePublishCheckItem.builder()
            .label("表达风险")
            .status(hasForbiddenExpression(content + title, request.getForbiddenExpressions()) ? "warn" : "pass")
            .detail(hasForbiddenExpression(content + title, request.getForbiddenExpressions()) ? "命中禁用表达，发布前请再润色" : "未发现禁用表达")
            .build());
        return checks;
    }

    private String buildTitleReason(VersionBlueprint blueprint, List<String> keywords, List<String> sellingPoints, int index) {
        if (index == 0) {
            return "用" + blueprint.getHookType() + "拉停留，并提前埋入“" + sellingPoints.get(0) + "”这个决策点。";
        }
        if (index == 1) {
            return "强化真实体验感，降低 AI 味，同时自然带出“" + keywords.get(0) + "”。";
        }
        return "适合做收藏型标题，用户一眼就知道这篇能解决选择问题。";
    }

    private List<String> fallbackList(List<String> values, List<String> defaults) {
        return values == null || values.isEmpty() ? defaults : values;
    }

    private String normalizeProductName(GenerateRequest request) {
        if (StringUtils.isNotBlank(request.getProductName())) {
            return request.getProductName();
        }
        return trimTitle(request.getCoreDescription());
    }

    private String normalizeStyleTag(String style) {
        if (StringUtils.isBlank(style)) {
            return "内容创作";
        }
        if ("good_item".equals(style)) {
            return "好物推荐";
        }
        if ("visit".equals(style)) {
            return "探店打卡";
        }
        if ("tutorial".equals(style)) {
            return "实用教程";
        }
        if ("collection".equals(style)) {
            return "清单合集";
        }
        if ("story".equals(style)) {
            return "真实故事";
        }
        return style;
    }

    private String sanitize(String text, List<String> forbiddenExpressions) {
        String safeText = StringUtils.defaultString(text);
        if (forbiddenExpressions == null) {
            return safeText;
        }
        for (String expression : forbiddenExpressions) {
            if (StringUtils.isNotBlank(expression)) {
                safeText = safeText.replace(expression, "***");
            }
        }
        return safeText;
    }

    private boolean hasForbiddenExpression(String text, List<String> forbiddenExpressions) {
        if (forbiddenExpressions == null || forbiddenExpressions.isEmpty()) {
            return false;
        }
        for (String expression : forbiddenExpressions) {
            if (StringUtils.isNotBlank(expression) && StringUtils.contains(text, expression)) {
                return true;
            }
        }
        return false;
    }

    private String trimTitle(String text) {
        String safeText = StringUtils.defaultIfBlank(text, "这篇内容");
        return safeText.length() > 16 ? safeText.substring(0, 16) : safeText;
    }

    private static class VersionBlueprint {

        private final String angleLabel;
        private final String hookType;
        private final String cta;

        private VersionBlueprint(String angleLabel, String hookType, String cta) {
            this.angleLabel = angleLabel;
            this.hookType = hookType;
            this.cta = cta;
        }

        public String getAngleLabel() {
            return angleLabel;
        }

        public String getHookType() {
            return hookType;
        }

        public String getCta() {
            return cta;
        }
    }
}
