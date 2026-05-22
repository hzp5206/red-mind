package com.redmind.modules.template.dto;

import lombok.Data;

@Data
public class TemplateQueryRequest {

    private String category;
    private String keyword;
    private Boolean isActive;
}
