package com.redmind.modules.log.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerationLogResponse {

    private Long id;
    private Long userId;
    private String coreInput;
    private String style;
    private String persona;
    private Integer wordCount;
    private Boolean collected;
    private String createdAt;
}
