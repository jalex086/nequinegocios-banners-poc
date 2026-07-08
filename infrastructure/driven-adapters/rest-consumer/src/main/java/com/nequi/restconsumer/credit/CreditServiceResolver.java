package com.nequi.restconsumer.credit;

import com.nequi.domain.model.banner.Banner;
import com.nequi.domain.model.banner.BannerConfig;
import com.nequi.domain.model.banner.UserContext;
import com.nequi.domain.model.gateway.BannerResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
public class CreditServiceResolver implements BannerResolver {

    private final WebClient webClient;
    private final int timeoutSeconds;

    public CreditServiceResolver(WebClient webClient, int timeoutSeconds) {
        this.webClient = webClient;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public String resolverType() {
        return "CREDIT_SERVICE";
    }

    @Override
    public Mono<Banner> resolve(BannerConfig config, UserContext user) {
        return webClient.post()
                .bodyValue(buildRequestBody(user))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnSubscribe(s -> log.info("Credit Service Request", kv("phone", user.phoneNumber())))
                .map(response -> mapResponse(response, config))
                .doOnNext(b -> log.info("Credit Service Response", kv("preApproved", b.metadata().get("preApproved"))))
                .onErrorResume(e -> {
                    log.warn("Credit Service Error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                    return Mono.just(buildFallbackBanner(config));
                });
    }

    private Banner mapResponse(String responseBody, BannerConfig config) {
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var response = mapper.readTree(responseBody);
            var status = response.at("/responseMessage/header/status/code").asText("");
            var products = response.at("/responseMessage/body/getProductIdRS/products");
            var channel = response.at("/responseMessage/body/getProductIdRS/approvalChannel").asText("");

            if ("0".equals(status) && products.isArray() && !products.isEmpty()) {
                double maxAmount = 0;
                for (var p : products) {
                    maxAmount = Math.max(maxAmount, p.get("amount").asDouble());
                }
                var t = config.template();
                return Banner.builder()
                        .id(config.bannerId())
                        .type("CREDIT")
                        .title(t.title().replace("${amount}", formatCurrency(maxAmount)))
                        .subtitle(t.subtitle())
                        .imageUrl(t.imageUrl())
                        .action(t.action())
                        .priority(config.priority())
                        .metadata(Map.of(
                                "maxAmount", maxAmount,
                                "productsCount", products.size(),
                                "approvalChannel", channel,
                                "preApproved", true))
                        .build();
            }
        } catch (Exception e) {
            log.warn("Error parsing credit response: {}", e.getMessage());
        }
        return buildFallbackBanner(config);
    }

    private Banner buildFallbackBanner(BannerConfig config) {
        var t = config.fallbackTemplate() != null ? config.fallbackTemplate() : config.template();
        return Banner.builder()
                .id(config.bannerId())
                .type("CREDIT")
                .title(t.title())
                .subtitle(t.subtitle())
                .imageUrl(t.imageUrl())
                .action(t.action())
                .priority(config.priority())
                .metadata(Map.of("preApproved", false))
                .build();
    }

    private String formatCurrency(double amount) {
        return "$" + String.format("%,.0f", amount);
    }

    private String buildRequestBody(UserContext user) {
        String customerId = user.documentType() + "-" + user.documentNumber();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return """
            {"requestMessage":{"header":{"messageID":"%s","requestDate":"%s",\
            "channel":{"id":"SOAT","name":"APP_NEGOCIOS"},\
            "consumer":{"address":{"deviceAddress":"0.0.0.0","networkAddress":"0.0.0.0"},"id":"%s","name":"phoneNumber"},\
            "container":{"id":"WLT","name":"Worklight"},\
            "destination":{"serviceName":"CreditsServices","serviceOperation":"creditDisbursement","serviceRegion":"C001","serviceVersion":"1.0.0"}},\
            "body":{"getProductIdRQ":{"productId":"4","customerId":"%s","phoneNumber":"%s"}}}}
            """.formatted(UUID.randomUUID(), now, user.phoneNumber(), customerId, user.phoneNumber()).trim();
    }
}
