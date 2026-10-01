package org.buyproxy;

import java.util.List;
import org.buyproxy.BuyProxy.Req;
import org.buyproxy.model.*;

public final class Balances extends Resource {
    Balances(BuyProxy c) { super(c); }

    /** Spendable GB per product, including GB still being added after a payment. */
    public List<Balance> list() { return c.readList(c.send(Req.get("/v1/balances")).get("balances"), Balance.class); }
}
