package com.redmind.modules.generate.service;

import com.redmind.common.config.RedMindAiProperties;
import com.redmind.common.exception.BizException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiProviderRouter {

    private final RedMindAiProperties aiProperties;
    private final List<AiProvider> providers;

    public AiProviderRouter(RedMindAiProperties aiProperties, List<AiProvider> providers) {
        this.aiProperties = aiProperties;
        this.providers = providers;
    }

    public AiProvider route() {
        String providerCode = normalizeProvider(aiProperties.getProvider());
        return providers.stream()
            .filter(provider -> provider.support(providerCode))
            .findFirst()
            .orElseThrow(() -> new BizException("未找到可用的 AI Provider: " + aiProperties.getProvider()));
    }

    private String normalizeProvider(String provider) {
        if ("deepseek".equalsIgnoreCase(provider)) {
            return "openai";
        }
        return provider;
    }
}
