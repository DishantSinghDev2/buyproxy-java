package org.buyproxy;

import org.jspecify.annotations.Nullable;

/** Base class for every exception thrown by the SDK. Unchecked, so it composes with lambdas and streams. */
public class BuyProxyException extends RuntimeException {
    private final @Nullable Integer status;
    private final @Nullable String body;

    public BuyProxyException(String message, @Nullable Integer status, @Nullable String body, @Nullable Throwable cause) {
        super(status != null ? message + " (HTTP " + status + ")" : message, cause);
        this.status = status;
        this.body = body;
    }

    /** HTTP status, or null when the request never got a response. */
    public @Nullable Integer status() { return status; }

    /** Raw response body, when there was one. */
    public @Nullable String body() { return body; }

    /** 400 — a parameter was missing or invalid. */
    public static final class Validation extends BuyProxyException {
        Validation(String m, int s, String b) { super(m, s, b, null); }
    }

    /** 401 — the API key is missing or invalid. */
    public static final class Authentication extends BuyProxyException {
        Authentication(String m, int s, @Nullable String b) { super(m, s, b, null); }
    }

    /** 402 — empty reseller pool, or a plan with no GB left. */
    public static final class InsufficientBandwidth extends BuyProxyException {
        InsufficientBandwidth(String m, int s, String b) { super(m, s, b, null); }
    }

    /** 403 — suspended account or customer, or the plan doesn't include this endpoint. */
    public static final class PermissionDenied extends BuyProxyException {
        PermissionDenied(String m, int s, String b) { super(m, s, b, null); }
    }

    /** 404 — the plan, customer or transaction doesn't exist on this account. */
    public static final class NotFound extends BuyProxyException {
        NotFound(String m, int s, String b) { super(m, s, b, null); }
    }

    /** 429 — more than 60 requests/minute on this key. */
    public static final class RateLimit extends BuyProxyException {
        private final @Nullable Double retryAfterSeconds;
        RateLimit(String m, String b, @Nullable Double retryAfter) { super(m, 429, b, null); this.retryAfterSeconds = retryAfter; }
        /** Seconds the API asked you to wait, when it said. */
        public @Nullable Double retryAfterSeconds() { return retryAfterSeconds; }
    }

    /** 5xx — BuyProxy or an upstream provider is unavailable; usually safe to retry. */
    public static final class Server extends BuyProxyException {
        Server(String m, int s, String b) { super(m, s, b, null); }
    }

    /** The request never got a response (DNS, reset, timeout, offline). */
    public static final class Connection extends BuyProxyException {
        Connection(String m, Throwable cause) { super(m, null, null, cause); }
    }

    static BuyProxyException fromResponse(int status, @Nullable String body, @Nullable String errorText, @Nullable Double retryAfter) {
        String message = errorText != null ? errorText : "BuyProxy API error";
        String b = body == null ? "" : body;
        return switch (status) {
            case 400 -> new Validation(message, status, b);
            case 401 -> new Authentication(message, status, b);
            case 402 -> new InsufficientBandwidth(message, status, b);
            case 403 -> new PermissionDenied(message, status, b);
            case 404 -> new NotFound(message, status, b);
            case 429 -> new RateLimit(message, b, retryAfter);
            default -> status >= 500 ? new Server(message, status, b) : new BuyProxyException(message, status, b, null);
        };
    }
}
