package org.buyproxy;

import org.buyproxy.BuyProxy.Req;
import org.buyproxy.model.*;

/** The public catalog. */
public final class Products extends Resource {
    Products(BuyProxy c) { super(c); }

    /** Every product with its {@code productKey}, minimum GB and live price. No API key needed. */
    public Catalog list() { return c.read(c.send(Req.get("/v1/products").asPublic()), Catalog.class); }
}
