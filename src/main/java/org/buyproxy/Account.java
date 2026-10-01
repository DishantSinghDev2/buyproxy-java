package org.buyproxy;

import org.buyproxy.BuyProxy.Req;
import org.buyproxy.model.*;

public final class Account extends Resource {
    Account(BuyProxy c) { super(c); }
    public org.buyproxy.model.Account get() { return c.read(c.send(Req.get("/v1/account")), org.buyproxy.model.Account.class); }
}
