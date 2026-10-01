package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** A Stripe checkout to open in a browser. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CardCheckout(
        String payUrl,
        @Nullable String transactionId) {}
