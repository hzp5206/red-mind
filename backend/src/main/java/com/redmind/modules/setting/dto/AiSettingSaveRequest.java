package com.redmind.modules.setting.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiSettingSaveRequest {

    @NotBlank(message = "AI provider 不能为空")
    private String provider;
    @NotBlank(message = "Base URL 不能为空")
    private String baseUrl;
    @NotBlank(message = "Model 不能为空")
    private String model;
    @NotBlank(message = "Chat Path 不能为空")
    private String chatPath;
    private String apiKey;
}
