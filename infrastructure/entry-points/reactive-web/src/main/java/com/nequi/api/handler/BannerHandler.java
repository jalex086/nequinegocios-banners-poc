package com.nequi.api.handler;

import com.nequi.api.dto.BannerResponse;
import com.nequi.domain.model.banner.UserContext;
import com.nequi.usecase.banner.GetBannersUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
@RequiredArgsConstructor
public class BannerHandler {

    private final GetBannersUseCase useCase;

    public Mono<ServerResponse> getBanners(ServerRequest request) {
        String context = request.queryParam("context").orElse("home");
        String requestId = UUID.randomUUID().toString();

        // In production: extract from JWT. In POC: headers.
        var user = UserContext.builder()
                .userId(request.headers().firstHeader("X-User-Id"))
                .role(headerOrDefault(request, "X-Role", "OWNER"))
                .issuer(headerOrDefault(request, "X-Issuer", "co-customer"))
                .phoneNumber(headerOrDefault(request, "X-Phone-Number", "3001460792"))
                .documentType(headerOrDefault(request, "X-Document-Type", "CC"))
                .documentNumber(headerOrDefault(request, "X-Document-Number", "52354338"))
                .build();

        log.info("Get Banners Request", kv("context", context), kv("role", user.role()));

        return useCase.execute(context, user)
                .map(banners -> BannerResponse.builder()
                        .code("BNR001")
                        .message("Successful Operation")
                        .identifier(requestId)
                        .date(Instant.now().toString())
                        .data(Map.of("banners", banners))
                        .build())
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnSuccess(r -> log.info("Get Banners Response", kv("requestId", requestId)));
    }

    private String headerOrDefault(ServerRequest request, String header, String defaultValue) {
        String value = request.headers().firstHeader(header);
        return value != null ? value : defaultValue;
    }
}
