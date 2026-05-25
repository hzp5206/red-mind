package com.redmind.modules.generate.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiRuntimeInfoResponse {

    private String provider;
    private String model;
    private String baseUrl;
}
