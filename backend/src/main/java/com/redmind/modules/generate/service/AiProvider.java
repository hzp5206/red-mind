package com.redmind.modules.generate.service;

import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GeneratedVersion;
import java.util.List;

public interface AiProvider {

    boolean support(String providerCode);

    List<GeneratedVersion> generate(GenerateRequest request);
}
