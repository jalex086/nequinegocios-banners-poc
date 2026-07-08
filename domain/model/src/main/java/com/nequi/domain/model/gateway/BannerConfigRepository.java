package com.nequi.domain.model.gateway;

import com.nequi.domain.model.banner.BannerConfig;
import reactor.core.publisher.Flux;

public interface BannerConfigRepository {
    Flux<BannerConfig> findByContext(String context);
}
