package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** GB allocated but still being added to a plan. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PendingAllocation(
        String productId,
        @Nullable String productKey,
        double gb) {}
