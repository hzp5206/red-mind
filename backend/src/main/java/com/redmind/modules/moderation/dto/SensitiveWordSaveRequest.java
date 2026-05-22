package com.redmind.modules.moderation.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SensitiveWordSaveRequest {

    private Long id;

    @NotBlank(message = "敏感词不能为空")
    private String word;

    @NotBlank(message = "替换词不能为空")
    private String replacement;

    private Boolean isActive = true;
}
