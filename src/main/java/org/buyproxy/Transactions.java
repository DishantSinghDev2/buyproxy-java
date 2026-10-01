package org.buyproxy;

import java.time.Duration;
import java.util.List;
import org.buyproxy.BuyProxy.Req;
import org.buyproxy.model.*;
import org.jspecify.annotations.Nullable;

import static org.buyproxy.BuyProxy.map;
import static org.buyproxy.BuyProxy.seg;

public final class Transactions extends Resource {
    Transactions(BuyProxy c) { super(c); }

    public List<Transaction> list(@Nullable Integer limit) {
        return c.readList(c.send(Req.get("/v1/transactions").withQuery(map("limit", limit))).get("transactions"), Transaction.class);
    }

    public Transaction get(String id) { return c.read(c.send(Req.get("/v1/transactions/" + seg(id))), Transaction.class); }

    /** Poll until the payment settles ({@code succeeded} or {@code failed}). */
    public Transaction await(String id, Duration interval, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (true) {
            Transaction tx = get(id);
            if (!"pending".equals(tx.status())) return tx;
            if (System.nanoTime() + interval.toNanos() > deadline) throw new BuyProxyException("Payment " + id + " is still pending after waiting", null, null, null);
            try {
                Thread.sleep(interval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BuyProxyException("Interrupted while waiting for payment " + id, null, null, e);
            }
        }
    }
}
