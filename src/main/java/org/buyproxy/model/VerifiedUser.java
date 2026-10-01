package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** The verified user. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VerifiedUser(
        String email,
        String name) {}
