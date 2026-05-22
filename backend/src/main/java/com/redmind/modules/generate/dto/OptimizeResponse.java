package com.redmind.modules.generate.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptimizeResponse {

    private String title;
    private String content;
    private List<String> tags;
}
