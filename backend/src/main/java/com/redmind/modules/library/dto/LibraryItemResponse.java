package com.redmind.modules.library.dto;

import java.util.List;
import lombok.Data;

@Data
public class LibraryItemResponse {

    private Long id;
    private Long historyId;
    private Long trendingItemId;
    private String sourceType;
    private String sourceTitle;
    private String customTags;
    private String productName;
    private String coreInput;
    private String previewText;
    private String style;
    private String tone;
    private String results;
    private String noteUrl;
    private String styleSample;
    private List<String> requiredKeywords;
    private String hookPreference;
    private String noteStructure;
    private String createdAt;
}
