package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** A subscription plan's price. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PlanPrice(
        String plan,
        int monthlyPriceCents,
        double includedGb) {}
