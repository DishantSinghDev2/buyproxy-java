package org.buyproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** The result of OTP verification. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VerifyResult(
        boolean ok,
        boolean isNew,
        VerifiedUser user) {}
