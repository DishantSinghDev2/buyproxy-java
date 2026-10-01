package org.buyproxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * BuyProxy API client. Thread-safe; create one and share it.
 *
 * <pre>{@code
 * BuyProxy bp = BuyProxy.builder().apiKey(System.getenv("BUYPROXY_API_KEY")).build();
 * GeneratedProxies out = bp.plans().generate(planId, GenerateParams.create().country("US").count(5));
 * HttpClient http = out.get(0).httpClientBuilder().build();
 * }</pre>
 */
public final class BuyProxy {
    public static final String VERSION = "1.0.0";
    public static final String DEFAULT_BASE_URL = "https://www.buyproxy.org";

    static final ObjectMapper JSON = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final @Nullable String apiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;
    private final HttpClient http;

    private final Products products = new Products(this);
    private final Auth auth = new Auth(this);
    private final Account account = new Account(this);
    private final Balances balances = new Balances(this);
    private final Plans plans = new Plans(this);
    private final Topup topup = new Topup(this);
    private final Transactions transactions = new Transactions(this);
    private final Reseller reseller = new Reseller(this);

    private BuyProxy(Builder b) {
        String key = b.apiKey != null ? b.apiKey : System.getenv("BUYPROXY_API_KEY");
        this.apiKey = key == null || key.isEmpty() ? null : key;
        this.baseUrl = b.baseUrl.replaceAll("/+$", "");
        this.timeout = b.timeout;
        this.maxRetries = b.maxRetries;
        this.http = b.httpClient != null ? b.httpClient : HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private @Nullable String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = 2;
        private @Nullable HttpClient httpClient;

        /** {@code bp_key_…} (personal) or {@code bp_live_…} (reseller). Defaults to {@code $BUYPROXY_API_KEY}. */
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public Builder timeout(Duration timeout) { this.timeout = timeout; return this; }
        /** Retries for rate limits and transient failures (default 2). */
        public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
        public Builder httpClient(HttpClient httpClient) { this.httpClient = httpClient; return this; }
        public BuyProxy build() { return new BuyProxy(this); }
    }

    public Products products() { return products; }
    public Auth auth() { return auth; }
    public Account account() { return account; }
    public Balances balances() { return balances; }
    public Plans plans() { return plans; }
    public Topup topup() { return topup; }
    public Transactions transactions() { return transactions; }
    public Reseller reseller() { return reseller; }

    /** {@code "reseller"} for bp_live_ keys, {@code "personal"} for bp_key_ keys, null without a key. */
    public @Nullable String keyKind() {
        if (apiKey == null) return null;
        return apiKey.startsWith("bp_live_") ? "reseller" : "personal";
    }

    /** Options for one request. */
    record Req(String method, String path, Map<String, Object> query, @Nullable Map<String, Object> body,
               Map<String, String> headers, boolean publicEndpoint, boolean retry5xx) {
        static Req get(String path) { return new Req("GET", path, Map.of(), null, Map.of(), false, true); }
        static Req post(String path, Map<String, Object> body) { return new Req("POST", path, Map.of(), body, Map.of(), false, false); }
        Req withQuery(Map<String, Object> q) { return new Req(method, path, q, body, headers, publicEndpoint, retry5xx); }
        Req asPublic() { return new Req(method, path, query, body, headers, true, retry5xx); }
        Req withMethod(String m) { return new Req(m, path, query, body, headers, publicEndpoint, retry5xx); }
        Req withHeaders(Map<String, String> h, boolean retry) { return new Req(method, path, query, body, h, publicEndpoint, retry); }
    }

    /** Sends a request and parses the JSON response. @hidden */
    JsonNode send(Req r) {
        if (!r.publicEndpoint() && apiKey == null) {
            throw new BuyProxyException.Authentication(
                    "No API key — use builder().apiKey(…) or set BUYPROXY_API_KEY. Create one at https://www.buyproxy.org/dashboard/api-keys", 401, null);
        }

        String query = r.query().entrySet().stream().filter(e -> e.getValue() != null)
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(e.getValue()), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        URI uri = URI.create(baseUrl + r.path() + (query.isEmpty() ? "" : "?" + query));

        String payload;
        try {
            payload = r.body() == null ? null : JSON.writeValueAsString(r.body());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }

        for (int attempt = 0; ; attempt++) {
            HttpRequest.Builder rb = HttpRequest.newBuilder(uri).timeout(timeout)
                    .header("Accept", "application/json").header("User-Agent", "buyproxy-java/" + VERSION);
            if (apiKey != null) rb.header("X-API-Key", apiKey);
            if (payload != null) rb.header("Content-Type", "application/json");
            r.headers().forEach(rb::header);
            rb.method(r.method(), payload == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(payload));

            HttpResponse<String> response;
            try {
                response = http.send(rb.build(), HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                if (r.retry5xx() && attempt < maxRetries) { sleep(backoffMillis(attempt)); continue; }
                throw new BuyProxyException.Connection("Could not reach BuyProxy: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BuyProxyException.Connection("Interrupted while calling BuyProxy", e);
            }

            int status = response.statusCode();
            String text = response.body() == null ? "" : response.body();
            if (status >= 200 && status < 300) {
                if (text.isEmpty()) return JSON.nullNode();
                try {
                    return JSON.readTree(text);
                } catch (JsonProcessingException e) {
                    throw new BuyProxyException("BuyProxy returned a non-JSON response (HTTP " + status + ")", status, text, e);
                }
            }

            Double retryAfter = response.headers().firstValue("retry-after").map(BuyProxy::parseSeconds).orElse(null);
            long wait = -1;
            if (attempt < maxRetries) {
                if (status == 429) wait = retryAfter != null ? (long) (retryAfter * 1000) : backoffMillis(attempt);
                else if (r.retry5xx() && (status == 502 || status == 503 || status == 504)) wait = backoffMillis(attempt);
            }
            if (wait >= 0) { sleep(wait); continue; }

            String errorText = null;
            try {
                JsonNode n = JSON.readTree(text);
                if (n != null && n.hasNonNull("error")) errorText = n.get("error").asText();
            } catch (IOException ignored) {
                // keep the generic message
            }
            throw BuyProxyException.fromResponse(status, text, errorText, retryAfter);
        }
    }

    <T> T read(JsonNode node, Class<T> type) {
        try {
            return JSON.treeToValue(node, type);
        } catch (JsonProcessingException e) {
            throw new BuyProxyException("Unexpected response from BuyProxy: " + e.getOriginalMessage(), null, node.toString(), e);
        }
    }

    <T> java.util.List<T> readList(JsonNode node, Class<T> type) {
        java.util.List<T> out = new java.util.ArrayList<>();
        for (JsonNode n : node) out.add(read(n, type));
        return out;
    }

    private static @Nullable Double parseSeconds(String v) {
        try {
            double s = Double.parseDouble(v);
            return s >= 0 ? Math.min(s, 30.0) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static long backoffMillis(int attempt) {
        return (long) (Math.min(500 * Math.pow(2, attempt), 8000) * (0.75 + Math.random() * 0.5));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) if (kv[i + 1] != null) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    static String seg(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20"); }
}
