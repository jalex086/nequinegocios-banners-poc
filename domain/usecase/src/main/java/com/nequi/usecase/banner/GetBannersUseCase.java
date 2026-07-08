package com.nequi.usecase.banner;

import com.nequi.domain.model.banner.Banner;
import com.nequi.domain.model.banner.BannerConfig;
import com.nequi.domain.model.banner.UserContext;
import com.nequi.domain.model.gateway.BannerConfigRepository;
import com.nequi.domain.model.gateway.BannerResolver;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetBannersUseCase {

    private final BannerConfigRepository configRepository;
    private final Map<String, BannerResolver> resolvers;

    public GetBannersUseCase(BannerConfigRepository configRepository, List<BannerResolver> resolverList) {
        this.configRepository = configRepository;
        this.resolvers = resolverList.stream()
                .collect(Collectors.toMap(BannerResolver::resolverType, Function.identity()));
    }

    public Mono<List<Banner>> execute(String context, UserContext user) {
        return configRepository.findByContext(context)
                .filter(BannerConfig::enabled)
                .filter(config -> matchesAudience(config, user))
                .flatMap(config -> resolveBanner(config, user))
                .collectSortedList(Comparator.comparingInt(Banner::priority));
    }

    private Mono<Banner> resolveBanner(BannerConfig config, UserContext user) {
        var resolver = resolvers.get(config.resolver());
        if (resolver == null) return Mono.empty();
        return resolver.resolve(config, user)
                .onErrorResume(e -> Mono.empty());
    }

    private boolean matchesAudience(BannerConfig config, UserContext user) {
        if (config.audience() == null) return true;
        var audience = config.audience();
        if (audience.roles() != null && !audience.roles().isEmpty()) {
            if (!audience.roles().contains(user.role())) return false;
        }
        if (audience.issuer() != null && !audience.issuer().isBlank()) {
            if (!user.issuer().contains(audience.issuer())) return false;
        }
        return true;
    }
}
