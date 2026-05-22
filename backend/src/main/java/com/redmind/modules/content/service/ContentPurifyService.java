package com.redmind.modules.content.service;

import com.redmind.modules.content.dto.PurifyResponse;
import com.redmind.modules.content.dto.WordReplacement;
import com.redmind.modules.moderation.service.SensitiveWordService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContentPurifyService {

    private final SensitiveWordService sensitiveWordService;

    public ContentPurifyService(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    public PurifyResponse purify(String content) {
        List<WordReplacement> replacements = new ArrayList<>();
        String cleanContent = content;
        for (String word : sensitiveWordService.detect(content)) {
            String replacement = sensitiveWordService.getReplacement(word);
            cleanContent = cleanContent.replace(word, replacement);
            replacements.add(new WordReplacement(word, replacement));
        }
        return new PurifyResponse(cleanContent, replacements);
    }
}
