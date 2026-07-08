package com.nequi.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Builder
@JsonInclude(NON_NULL)
public record BannerResponse(
    String code,
    String message,
    String identifier,
    String date,
    Map<String, Object> data
) {}
