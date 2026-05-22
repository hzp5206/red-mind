package com.redmind.modules.category.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemplateCategoryOptionResponse {

    private Long id;
    private String label;
    private String value;
    private Integer sortOrder;
    private Boolean isActive;
}
