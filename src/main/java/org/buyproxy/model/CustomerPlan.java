package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** A plan plus the customer's remaining GB. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerPlan(
        String id,
        String productId,
        @Nullable String productKey,
        String name,
        @Nullable String label,
        double dataGbRemaining,
        double dataGbTotal,
        @Nullable String expiresAt,
        String createdAt,
        @Nullable GenerateCapabilities generate,
        double remainingGb) {}
