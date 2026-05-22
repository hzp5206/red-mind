package com.redmind.modules.generate.dto;

import java.util.List;
import lombok.Data;

@Data
public class AiGeneratedPayload {

    private List<GeneratedVersion> versions;
}
