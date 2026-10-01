package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** Unallocated reseller bandwidth on one product. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PoolEntry(
        String productKey,
        String productId,
        String label,
        double gb) {}
