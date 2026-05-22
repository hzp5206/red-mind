package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GeneratedVersion;
import com.redmind.modules.generate.dto.QualityScores;
import com.redmind.modules.moderation.service.SensitiveWordService;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class QualityScoreService {

    private final SensitiveWordService sensitiveWordService;

    public QualityScoreService(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    public QualityScores score(GenerateRequest request, GeneratedVersion version) {
        double titleScore = 3.5D;
        String title = version.getTitle();
        if (title.contains("！") || title.contains("?") || title.contains("？")) {
            titleScore += 0.5D;
        }
        if (title.matches(".*[0-9一二三四五六七八九十].*")) {
            titleScore += 0.5D;
        }
        if (title.length() <= 20) {
            titleScore += 0.5D;
        }

        int hitCount = 0;
        if (request.getRequiredKeywords() != null) {
            for (String keyword : request.getRequiredKeywords()) {
                if (StringUtils.contains(version.getContent(), keyword) || StringUtils.contains(version.getTitle(), keyword)) {
                    hitCount++;
                }
            }
        }
        String density = hitCount >= 3 ? "rich" : hitCount >= 1 ? "good" : "normal";

        List<String> issues = new ArrayList<>();
        List<String> sensitiveWords = sensitiveWordService.detect(version.getContent() + version.getTitle());
        if (sensitiveWords.isEmpty()) {
            issues.add("已净化");
        } else {
            for (String word : sensitiveWords) {
                issues.add("检测到敏感表达：" + word);
            }
        }

        return QualityScores.builder()
            .titleAttraction(Math.min(titleScore, 5.0D))
            .keywordDensity(density)
            .complianceIssues(issues)
            .build();
    }
}
