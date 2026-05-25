package com.redmind.modules.trending.provider;

import com.redmind.modules.trending.entity.TrendingCopyTask;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MockTrendingProvider implements TrendingProvider {

    @Override
    public boolean supports(String providerCode) {
        return "mock".equalsIgnoreCase(providerCode);
    }

    @Override
    public List<TrendingSourceItem> fetch(TrendingCopyTask task) {
        List<TrendingSourceItem> items = new ArrayList<>();
        String[] keywords = StringUtils.defaultIfBlank(task.getKeywords(), "护肤").split("[,，\\s]+");
        int limit = task.getFetchLimit() == null ? 10 : task.getFetchLimit();
        for (int index = 0; index < limit; index++) {
            String keyword = keywords[index % keywords.length];
            int likes = ThreadLocalRandom.current().nextInt(1200, 12000);
            int collects = ThreadLocalRandom.current().nextInt(500, 8000);
            int comments = ThreadLocalRandom.current().nextInt(100, 2000);
            items.add(TrendingSourceItem.builder()
                .sourceId(task.getPlatformCode() + "_" + keyword + "_" + index)
                .keyword(keyword)
                .title(buildTitle(keyword, index))
                .contentText(buildContent(keyword, index))
                .authorName("爆文观察员" + (index + 1))
                .noteUrl("https://www.xiaohongshu.com/explore/mock-" + keyword + "-" + index)
                .likesCount(likes)
                .collectsCount(collects)
                .commentsCount(comments)
                .heatScore(likes + collects * 2 + comments * 3)
                .tags(Arrays.asList("#" + keyword, "#爆款拆解", "#选题灵感"))
                .publishedAt(LocalDateTime.now().minusHours(index + 1L))
                .build());
        }
        return items;
    }

    private String buildTitle(String keyword, int index) {
        String[] hooks = {
            "这条" + keyword + "笔记为什么能爆？",
            keyword + "内容这么写，收藏率真的高",
            "做" + keyword + "赛道，先抄这类爆文结构",
            keyword + "笔记的高赞开头，我帮你拆好了"
        };
        return hooks[index % hooks.length];
    }

    private String buildContent(String keyword, int index) {
        return "这是一条围绕“" + keyword + "”生成的模拟爆文样本，用于采集中心联调。\n\n"
            + "它会强调高停留开头、清单式结构、评论引导和收藏点，方便你后面直接进入选题池、爆文库和文案生成器复用。\n\n"
            + "第 " + (index + 1) + " 条样本重点展示了“标题、结构、互动设计”三个维度。";
    }
}
