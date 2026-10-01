package org.buyproxy;

import org.buyproxy.BuyProxy.Req;
import org.buyproxy.model.*;

/** Reseller API — needs a {@code bp_live_} key on a Business plan or higher. */
public final class Reseller extends Resource {
    private final Customers customers;
    private final Pool pool;

    Reseller(BuyProxy c) { super(c); this.customers = new Customers(c); this.pool = new Pool(c); }

    public ResellerSummary me() { return c.read(c.send(Req.get("/v1/me")), ResellerSummary.class); }
    public Customers customers() { return customers; }
    public Pool pool() { return pool; }
}
