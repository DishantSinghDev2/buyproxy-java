package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** Spendable bandwidth on one product. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Balance(
        @Nullable String productKey,
        String productId,
        @Nullable String planId,
        double activeGb,
        double pendingGb,
        double totalGb,
        @Nullable String expiresAt) {}
