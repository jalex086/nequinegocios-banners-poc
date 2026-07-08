package com.nequi.domain.model.banner;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record BannerConfig(
    String context,
    String bannerId,
    String resolver,      // STATIC, CREDIT_SERVICE, HTTP_SERVICE
    int priority,
    boolean enabled,
    Audience audience,
    Template template,
    Template fallbackTemplate
) {
    @Builder(toBuilder = true)
    public record Audience(List<String> roles, String issuer) {}

    @Builder(toBuilder = true)
    public record Template(String title, String subtitle, String imageUrl, String action) {}
}
