package com.redmind.modules.content.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PurifyRequest {

    @NotBlank(message = "内容不能为空")
    private String content;
}
