package com.nequi.restconsumer.config;

import com.nequi.restconsumer.credit.CreditServiceResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CreditServiceConfig {

    @Bean
    public CreditServiceResolver creditServiceResolver(
            @Value("${banners.credits.base-url}") String baseUrl,
            @Value("${banners.credits.path}") String path,
            @Value("${banners.credits.api-key}") String apiKey,
            @Value("${banners.credits.api-gw-id}") String apiGwId,
            @Value("${banners.credits.timeout-seconds}") int timeout) {

        var httpClient = reactor.netty.http.client.HttpClient.create()
                .resolver(io.netty.resolver.DefaultAddressResolverGroup.INSTANCE);

        WebClient webClient = WebClient.builder()
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl + path)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("x-apigw-api-id", apiGwId)
                .defaultHeader("Content-Type", "application/json")
                .build();

        return new CreditServiceResolver(webClient, timeout);
    }
}
