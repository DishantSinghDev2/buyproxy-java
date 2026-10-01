package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** Bandwidth you hold on one product. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Plan(
        String id,
        String productId,
        @Nullable String productKey,
        String name,
        @Nullable String label,
        double dataGbRemaining,
        double dataGbTotal,
        @Nullable String expiresAt,
        String createdAt,
        @Nullable GenerateCapabilities generate) {}
