package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** The result of an allocation. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Allocation(
        String productKey,
        String productId,
        int gb,
        boolean alreadyApplied,
        double poolRemainingGb) {}
