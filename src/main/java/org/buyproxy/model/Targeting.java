package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** Which targeting fields a plan supports. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Targeting(
        boolean country,
        boolean state,
        boolean city,
        boolean isp) {}
