package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** The account behind the API key. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Account(
        String email,
        String name,
        String plan,
        @Nullable String planExpiresAt,
        double balanceGb,
        String createdAt) {}
