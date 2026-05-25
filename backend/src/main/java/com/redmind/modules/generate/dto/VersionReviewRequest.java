package com.redmind.modules.generate.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VersionReviewRequest {

    @Valid
    @NotNull(message = "生成上下文不能为空")
    private GenerateRequest request;

    @Valid
    @NotNull(message = "版本内容不能为空")
    private GeneratedVersion version;
}
