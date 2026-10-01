package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** Consumption on one plan. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsagePlan(
        String id,
        String productId,
        @Nullable String productKey,
        double remainingGb,
        double totalGb,
        double usedGb,
        @Nullable String expiresAt) {}
