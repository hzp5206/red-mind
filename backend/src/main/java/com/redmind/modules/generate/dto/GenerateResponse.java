package com.redmind.modules.generate.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateResponse {

    private Long historyId;
    private String generationId;
    private List<GeneratedVersion> versions;
    private Integer creditsUsed;
}
