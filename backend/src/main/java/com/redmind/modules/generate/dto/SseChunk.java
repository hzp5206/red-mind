package com.redmind.modules.generate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SseChunk {

    private Integer ver;
    private String text;
}
