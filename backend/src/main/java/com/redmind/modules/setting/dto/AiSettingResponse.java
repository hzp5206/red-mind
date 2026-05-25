package com.redmind.modules.setting.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiSettingResponse {

    private String provider;
    private String baseUrl;
    private String model;
    private String chatPath;
    private String apiKeyValue;
    private String apiKeyMasked;
    private Boolean apiKeyConfigured;
    private List<String> providerOptions;
    private List<String> modelOptions;
}
