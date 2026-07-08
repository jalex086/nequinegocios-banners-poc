package com.nequi.domain.model.banner;

import lombok.Builder;

import java.util.Map;

@Builder(toBuilder = true)
public record Banner(
    String id,
    String type,
    String title,
    String subtitle,
    String imageUrl,
    String action,
    int priority,
    Map<String, Object> metadata
) {}
