package org.buyproxy;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.buyproxy.BuyProxy.Req;
import org.buyproxy.model.*;

import static org.buyproxy.BuyProxy.seg;

public final class Plans extends Resource {
    Plans(BuyProxy c) { super(c); }

    public List<Plan> list() { return c.readList(c.send(Req.get("/v1/plans")).get("plans"), Plan.class); }

    /** Generate proxy lines against one of your plans. Lines are on-demand — don't store them. */
    public GeneratedProxies generate(String planId, GenerateParams params) {
        JsonNode r = c.send(Req.post("/v1/plans/" + seg(planId) + "/generate", params.toBody()));
        return GeneratedProxies.of(lines(r), params.protocolOrNull() == null ? "http" : params.protocolOrNull());
    }

    public GeneratedProxies generate(String planId) { return generate(planId, GenerateParams.create()); }

    static List<String> lines(JsonNode r) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        r.get("lines").forEach(n -> out.add(n.asText()));
        return out;
    }
}
