package com.redmind.modules.trending.provider;

import com.redmind.modules.trending.entity.TrendingCopyTask;
import java.util.List;

public interface TrendingProvider {

    boolean supports(String providerCode);

    List<TrendingSourceItem> fetch(TrendingCopyTask task);
}
