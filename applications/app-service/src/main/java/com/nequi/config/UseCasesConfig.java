package com.nequi.config;

import com.nequi.domain.model.gateway.BannerConfigRepository;
import com.nequi.domain.model.gateway.BannerResolver;
import com.nequi.usecase.banner.GetBannersUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class UseCasesConfig {

    @Bean
    public GetBannersUseCase getBannersUseCase(BannerConfigRepository configRepository,
                                               List<BannerResolver> resolvers) {
        return new GetBannersUseCase(configRepository, resolvers);
    }
}
