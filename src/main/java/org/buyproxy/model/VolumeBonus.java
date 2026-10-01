package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** Top-ups of thresholdGb or more get floor(gb × rate) extra GB free. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VolumeBonus(
        int thresholdGb,
        double rate) {}
