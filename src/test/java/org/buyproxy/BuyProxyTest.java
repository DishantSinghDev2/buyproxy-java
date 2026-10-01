package org.buyproxy;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.buyproxy.model.Allocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BuyProxyTest {
    record Seen(String method, String path, String body, Map<String, String> headers) {}

    record Reply(int status, String json, Map<String, String> headers) {
        Reply(int status, String json) { this(status, json, Map.of()); }
    }

    private HttpServer server;
    private final List<Seen> seen = new ArrayList<>();

    @AfterEach void stop() { if (server != null) server.stop(0); }

    private BuyProxy client(Function<Seen, Reply> handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ex -> {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> h = new java.util.HashMap<>();
            ex.getRequestHeaders().forEach((k, v) -> h.put(k.toLowerCase(), v.get(0)));
            Seen s = new Seen(ex.getRequestMethod(), ex.getRequestURI().getRawPath() + (ex.getRequestURI().getRawQuery() != null ? "?" + ex.getRequestURI().getRawQuery() : ""), body, h);
            seen.add(s);
            Reply r = handler.apply(s);
            r.headers().forEach((k, v) -> ex.getResponseHeaders().add(k, v));
            byte[] out = r.json().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(r.status(), out.length);
            ex.getResponseBody().write(out);
            ex.close();
        });
        server.start();
        return BuyProxy.builder().apiKey("bp_key_x").baseUrl("http://127.0.0.1:" + server.getAddress().getPort()).build();
    }

    private static final String ACCOUNT = "{\"email\":\"a@b.co\",\"name\":\"A\",\"plan\":\"pro\",\"planExpiresAt\":null,\"balanceGb\":4.2,\"createdAt\":\"t\",\"brandNew\":1}";

    @Test void proxyParsingKeepsAtAndColonInPassword() {
        String line = "user-country-us:p@ss:w0rd@gw.buyproxy.org:6969";
        ProxyEndpoint p = ProxyEndpoint.parse(line);
        assertEquals("gw.buyproxy.org", p.host());
        assertEquals(6969, p.port());
        assertEquals("p@ss:w0rd", p.password());
        assertEquals(line, p.toString());
        assertEquals("http://user-country-us:p%40ss%3Aw0rd@gw.buyproxy.org:6969", p.url());
        assertEquals("socks5://h.example:1080", ProxyEndpoint.parse("u:p@h.example:1080", "socks5").server());
    }

    @Test void proxyParsingRejectsMalformedLines() {
        for (String bad : new String[] {"", "nope", "u:p@host", "u:p@host:99999", ":p@host:80", "u@host:80"})
            assertThrows(IllegalArgumentException.class, () -> ProxyEndpoint.parse(bad), bad);
    }

    @Test void generateSendsKeyBodyAndParses() throws Exception {
        BuyProxy bp = client(s -> new Reply(200, "{\"lines\":[\"u:p@gw.buyproxy.org:6969\"]}"));
        GeneratedProxies out = bp.plans().generate("plan 1", GenerateParams.create().protocol("socks5").count(1));
        Seen s = seen.get(0);
        assertEquals("/v1/plans/plan%201/generate", s.path());
        assertEquals("bp_key_x", s.headers().get("x-api-key"));
        assertTrue(s.headers().get("user-agent").startsWith("buyproxy-java/"));
        assertEquals("{\"protocol\":\"socks5\",\"count\":1}", s.body());
        assertEquals("socks5", out.get(0).protocol());
    }

    @Test void modelsAreTypedAndIgnoreUnknownFields() throws Exception {
        BuyProxy bp = client(s -> new Reply(200, ACCOUNT));
        var a = bp.account().get();
        assertEquals(4.2, a.balanceGb());
        assertNull(a.planExpiresAt());
    }

    @Test void statusesMapToExceptions() throws Exception {
        Map<Integer, Class<? extends BuyProxyException>> cases = Map.of(
                401, BuyProxyException.Authentication.class, 402, BuyProxyException.InsufficientBandwidth.class, 404, BuyProxyException.NotFound.class);
        for (var e : cases.entrySet()) {
            BuyProxy bp = client(s -> new Reply(e.getKey(), "{\"error\":\"nope\"}"));
            BuyProxyException ex = assertThrows(e.getValue(), () -> bp.account().get());
            assertEquals(e.getKey(), ex.status());
            assertTrue(ex.getMessage().contains("nope"));
            server.stop(0);
        }
    }

    @Test void retries429ThenSucceeds() throws Exception {
        AtomicInteger n = new AtomicInteger();
        BuyProxy bp = client(s -> n.incrementAndGet() == 1 ? new Reply(429, "{\"error\":\"slow\"}", Map.of("Retry-After", "0")) : new Reply(200, ACCOUNT));
        assertEquals("pro", bp.account().get().plan());
        assertEquals(2, seen.size());
    }

    @Test void postNotRetriedOn503UnlessIdempotent() throws Exception {
        BuyProxy bp = client(s -> new Reply(503, "{\"error\":\"down\"}"));
        assertThrows(BuyProxyException.Server.class, () -> bp.reseller().customers().allocate("c", "premium", 1, null));
        assertEquals(1, seen.size());

        seen.clear();
        server.stop(0);
        AtomicInteger n = new AtomicInteger();
        BuyProxy keyed = client(s -> n.incrementAndGet() < 2 ? new Reply(503, "{\"error\":\"down\"}")
                : new Reply(201, "{\"productKey\":\"premium\",\"productId\":\"x\",\"gb\":1,\"alreadyApplied\":false,\"poolRemainingGb\":0}"));
        Allocation a = keyed.reseller().customers().allocate("c", "premium", 1, "k1");
        assertFalse(a.alreadyApplied());
        assertEquals("k1", seen.get(1).headers().get("idempotency-key"));
        assertEquals("{\"productKey\":\"premium\",\"gb\":1}", seen.get(1).body());
    }

    @Test void missingKeyNeverHitsNetworkButCatalogIsPublic() throws Exception {
        client(s -> new Reply(200, "{\"products\":[],\"volumeBonus\":{\"thresholdGb\":100,\"rate\":0.1},\"plans\":[]}"));
        BuyProxy anon = BuyProxy.builder().apiKey("").baseUrl("http://127.0.0.1:" + server.getAddress().getPort()).build();
        if (System.getenv("BUYPROXY_API_KEY") != null) return;
        assertThrows(BuyProxyException.Authentication.class, () -> anon.account().get());
        assertEquals(100, anon.products().list().volumeBonus().thresholdGb());
        assertEquals(1, seen.size());
    }

    @Test void awaitPollsUntilSettled() throws Exception {
        AtomicInteger n = new AtomicInteger();
        BuyProxy bp = client(s -> new Reply(200, "{\"id\":\"st_1\",\"kind\":\"topup\",\"status\":\"" + (n.incrementAndGet() < 2 ? "pending" : "succeeded")
                + "\",\"paymentMethod\":\"card\",\"interval\":null,\"amountCents\":1,\"currency\":\"usd\",\"gb\":1,\"productKey\":null,\"plan\":null,\"resellerPool\":false,\"createdAt\":\"t\"}"));
        assertEquals("succeeded", bp.transactions().await("st_1", Duration.ofMillis(5), Duration.ofSeconds(2)).status());
    }

    @Test void keyKind() {
        assertEquals("reseller", BuyProxy.builder().apiKey("bp_live_x").build().keyKind());
        assertEquals("personal", BuyProxy.builder().apiKey("bp_key_x").build().keyKind());
    }
}
