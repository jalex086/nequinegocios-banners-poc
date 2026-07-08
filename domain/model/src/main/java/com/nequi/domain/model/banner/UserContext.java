package com.nequi.domain.model.banner;

import lombok.Builder;

@Builder(toBuilder = true)
public record UserContext(
    String userId,
    String role,
    String issuer,
    String phoneNumber,
    String documentType,
    String documentNumber
) {}
