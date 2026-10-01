package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** One payment. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Transaction(
        String id,
        String kind,
        String status,
        String paymentMethod,
        @Nullable String interval,
        int amountCents,
        String currency,
        @Nullable Double gb,
        @Nullable String productKey,
        @Nullable String plan,
        boolean resellerPool,
        String createdAt) {}
