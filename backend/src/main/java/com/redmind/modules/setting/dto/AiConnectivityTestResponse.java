package com.redmind.modules.setting.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiConnectivityTestResponse {

    private Boolean success;
    private String provider;
    private String model;
    private String baseUrl;
    private String message;
}
