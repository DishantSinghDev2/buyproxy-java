package org.buyproxy.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** A customer with their plans. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerDetail(
        String username,
        String label,
        String status,
        String createdAt,
        List<CustomerPlan> plans,
        List<PendingAllocation> pending) {}
