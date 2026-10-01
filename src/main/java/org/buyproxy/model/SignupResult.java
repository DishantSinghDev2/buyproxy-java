package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** The result of signup. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SignupResult(
        boolean ok,
        @Nullable Integer cooldownSeconds) {}
