package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** A product in the catalog. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Product(
        String productKey,
        String productId,
        String name,
        String description,
        int minGb,
        @Nullable Integer pricePerGbCents,
        String currency) {}
