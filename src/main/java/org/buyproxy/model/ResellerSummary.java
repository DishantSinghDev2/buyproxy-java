package org.buyproxy.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** Reseller account summary. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResellerSummary(
        String email,
        int customerCount,
        int activeCustomerCount,
        List<PoolEntry> pool) {}
