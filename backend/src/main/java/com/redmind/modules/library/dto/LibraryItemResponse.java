package com.redmind.modules.library.dto;

import lombok.Data;

@Data
public class LibraryItemResponse {

    private Long id;
    private Long historyId;
    private String customTags;
    private String coreInput;
    private String style;
    private String results;
    private String createdAt;
}
