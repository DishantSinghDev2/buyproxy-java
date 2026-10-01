package org.buyproxy.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** A snapshot of a customer's consumption. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerUsage(
        List<UsagePlan> plans,
        List<PendingAllocation> pending) {}
