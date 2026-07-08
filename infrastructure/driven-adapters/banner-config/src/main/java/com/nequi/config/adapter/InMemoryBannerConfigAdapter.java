package com.nequi.config.adapter;

import com.nequi.domain.model.banner.BannerConfig;
import com.nequi.domain.model.banner.BannerConfig.Audience;
import com.nequi.domain.model.banner.BannerConfig.Template;
import com.nequi.domain.model.gateway.BannerConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory adapter that simulates DynamoDB banner-config table.
 * In production this would be replaced by a DynamoDB adapter.
 */
@Slf4j
@Repository
public class InMemoryBannerConfigAdapter implements BannerConfigRepository {

    private final Map<String, List<BannerConfig>> store = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        store.put("home", List.of(
            BannerConfig.builder()
                .context("home").bannerId("credit-preapproval")
                .resolver("CREDIT_SERVICE").priority(1).enabled(true)
                .audience(Audience.builder().roles(List.of("OWNER")).issuer("co-customer").build())
                .template(Template.builder()
                    .title("Tienes un crédito pre-aprobado por ${amount}")
                    .subtitle("Solicítalo ahora sin papeleos")
                    .imageUrl("https://cdn.nequi.com/banners/credit-negocios.png")
                    .action("deeplink://credits/apply").build())
                .fallbackTemplate(Template.builder()
                    .title("Solicita tu crédito para hacer crecer tu negocio")
                    .subtitle("Conoce las opciones disponibles")
                    .imageUrl("https://cdn.nequi.com/banners/credit-generic.png")
                    .action("deeplink://credits/info").build())
                .build(),
            BannerConfig.builder()
                .context("home").bannerId("promo-qr")
                .resolver("STATIC").priority(3).enabled(true)
                .audience(Audience.builder().roles(List.of("OWNER", "ADMIN")).build())
                .template(Template.builder()
                    .title("Activa tu QR y cobra sin efectivo")
                    .subtitle("Gratis para tu negocio")
                    .imageUrl("https://cdn.nequi.com/banners/qr-promo.png")
                    .action("deeplink://qr/activate").build())
                .build(),
            BannerConfig.builder()
                .context("home").bannerId("seller-tips")
                .resolver("STATIC").priority(5).enabled(true)
                .audience(Audience.builder().roles(List.of("SELLER")).build())
                .template(Template.builder()
                    .title("Revisa tus ventas del día")
                    .subtitle("Accede al resumen rápido")
                    .imageUrl("https://cdn.nequi.com/banners/seller-tips.png")
                    .action("deeplink://movements").build())
                .build()
        ));

        store.put("payments", List.of(
            BannerConfig.builder()
                .context("payments").bannerId("link-pago-promo")
                .resolver("STATIC").priority(1).enabled(true)
                .template(Template.builder()
                    .title("Crea links de pago y cobra a distancia")
                    .subtitle("Sin costo adicional")
                    .imageUrl("https://cdn.nequi.com/banners/payment-link.png")
                    .action("deeplink://payment-links/create").build())
                .build()
        ));

        log.info("Banner config loaded: {} contexts, {} banners total",
                store.size(), store.values().stream().mapToInt(List::size).sum());
    }

    @Override
    public Flux<BannerConfig> findByContext(String context) {
        return Flux.fromIterable(store.getOrDefault(context, List.of()));
    }
}
