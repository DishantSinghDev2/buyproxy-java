package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** A crypto invoice to open in a browser. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CryptoCheckout(
        String url,
        String transactionId) {}
