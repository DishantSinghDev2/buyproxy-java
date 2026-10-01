package org.buyproxy;

import java.util.List;
import org.buyproxy.BuyProxy.Req;
import org.buyproxy.model.*;

import static org.buyproxy.BuyProxy.map;

public final class Pool extends Resource {
    Pool(BuyProxy c) { super(c); }

    /** Your unallocated bandwidth, per product. */
    public List<PoolEntry> get() { return c.readList(c.send(Req.get("/v1/pool")).get("pool"), PoolEntry.class); }

    public CardCheckout topupCard(String productKey, int gb) {
        return c.read(c.send(Req.post("/v1/pool/topup/card", map("productKey", productKey, "gb", gb))), CardCheckout.class);
    }

    public CryptoCheckout topupCrypto(String productKey, int gb) {
        return c.read(c.send(Req.post("/v1/pool/topup/crypto", map("productKey", productKey, "gb", gb))), CryptoCheckout.class);
    }
}
