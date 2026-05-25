package com.redmind.modules.trending.provider;

import com.redmind.common.exception.BizException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TrendingProviderRouter {

    private final List<TrendingProvider> providers;

    public TrendingProviderRouter(List<TrendingProvider> providers) {
        this.providers = providers;
    }

    public TrendingProvider route(String providerCode) {
        return providers.stream()
            .filter(provider -> provider.supports(providerCode))
            .findFirst()
            .orElseThrow(() -> new BizException("未找到可用的采集 Provider：" + providerCode));
    }
}
