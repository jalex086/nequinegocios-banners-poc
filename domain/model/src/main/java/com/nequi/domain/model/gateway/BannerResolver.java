package com.nequi.domain.model.gateway;

import com.nequi.domain.model.banner.Banner;
import com.nequi.domain.model.banner.BannerConfig;
import com.nequi.domain.model.banner.UserContext;
import reactor.core.publisher.Mono;

public interface BannerResolver {
    String resolverType();
    Mono<Banner> resolve(BannerConfig config, UserContext user);
}
