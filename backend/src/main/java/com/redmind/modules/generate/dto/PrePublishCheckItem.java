package com.redmind.modules.generate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrePublishCheckItem {

    private String label;
    private String status;
    private String detail;
}
