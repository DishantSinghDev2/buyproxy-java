package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** A reseller sub-account. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Customer(
        String username,
        String label,
        String status,
        String createdAt) {}
