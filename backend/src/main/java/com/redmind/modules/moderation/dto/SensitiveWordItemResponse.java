package com.redmind.modules.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SensitiveWordItemResponse {

    private Long id;
    private String word;
    private String replacement;
    private Boolean isActive;
}
