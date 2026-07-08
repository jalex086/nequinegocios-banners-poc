package com.nequi.config.adapter;

import com.nequi.domain.model.banner.Banner;
import com.nequi.domain.model.banner.BannerConfig;
import com.nequi.domain.model.banner.UserContext;
import com.nequi.domain.model.gateway.BannerResolver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StaticBannerResolver implements BannerResolver {

    @Override
    public String resolverType() {
        return "STATIC";
    }

    @Override
    public Mono<Banner> resolve(BannerConfig config, UserContext user) {
        var t = config.template();
        return Mono.just(Banner.builder()
                .id(config.bannerId())
                .type("PROMO")
                .title(t.title())
                .subtitle(t.subtitle())
                .imageUrl(t.imageUrl())
                .action(t.action())
                .priority(config.priority())
                .build());
    }
}
