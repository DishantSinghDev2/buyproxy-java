package org.buyproxy;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.buyproxy.BuyProxy.Req;
import org.buyproxy.model.*;
import org.jspecify.annotations.Nullable;

import static org.buyproxy.BuyProxy.map;
import static org.buyproxy.BuyProxy.seg;

public final class Customers extends Resource {
    Customers(BuyProxy c) { super(c); }

    /** @param status {@code "active"}, {@code "suspended"}, or null for all */
    public List<Customer> list(@Nullable String status) {
        return c.readList(c.send(Req.get("/v1/customers").withQuery(map("status", status))).get("customers"), Customer.class);
    }

    /** Creates an isolated sub-account with no bandwidth — fund it with {@link #allocate}. */
    public Customer create(String label) { return c.read(c.send(Req.post("/v1/customers", map("label", label))), Customer.class); }

    public CustomerDetail get(String username) { return c.read(c.send(Req.get("/v1/customers/" + seg(username))), CustomerDetail.class); }

    /** Rename and/or suspend ({@code "suspended"}) / reactivate ({@code "active"}); pass null to leave a field unchanged. */
    public Customer update(String username, @Nullable String label, @Nullable String status) {
        Req r = Req.post("/v1/customers/" + seg(username), map("label", label, "status", status)).withMethod("PATCH");
        return c.read(c.send(r), Customer.class);
    }

    public CustomerUsage usage(String username) { return c.read(c.send(Req.get("/v1/customers/" + seg(username) + "/usage")), CustomerUsage.class); }

    /** Moves GB from your pool onto a customer. Pass {@code idempotencyKey} to make retries safe. */
    public Allocation allocate(String username, String productKey, int gb, @Nullable String idempotencyKey) {
        Req r = Req.post("/v1/customers/" + seg(username) + "/allocate", map("productKey", productKey, "gb", gb))
                .withHeaders(idempotencyKey != null ? Map.of("Idempotency-Key", idempotencyKey) : Map.of(), idempotencyKey != null);
        return c.read(c.send(r), Allocation.class);
    }

    public GeneratedProxies generate(String username, String planId, GenerateParams params) {
        Map<String, Object> body = map("planId", planId);
        body.putAll(params.toBody());
        JsonNode r = c.send(Req.post("/v1/customers/" + seg(username) + "/generate", body));
        return GeneratedProxies.of(Plans.lines(r), params.protocolOrNull() == null ? "http" : params.protocolOrNull());
    }
}
