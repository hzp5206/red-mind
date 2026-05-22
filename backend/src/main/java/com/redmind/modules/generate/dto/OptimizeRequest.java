package com.redmind.modules.generate.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OptimizeRequest {

    @NotBlank(message = "优化类型不能为空")
    private String option;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String content;

    private List<String> tags;
}
