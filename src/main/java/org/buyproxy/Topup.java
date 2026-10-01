package org.buyproxy;

import org.buyproxy.BuyProxy.Req;
import org.buyproxy.model.*;

import static org.buyproxy.BuyProxy.map;

public final class Topup extends Resource {
    Topup(BuyProxy c) { super(c); }

    /** Stripe checkout — a top-up or a plan subscription. Open {@code payUrl} to pay. */
    public CardCheckout card(CardCheckoutParams params) { return c.read(c.send(Req.post("/v1/topup/card", params.toBody())), CardCheckout.class); }

    /** Crypto top-up (pay-as-you-go only). Open {@code url} to pay. */
    public CryptoCheckout crypto(String productKey, int gb) {
        return c.read(c.send(Req.post("/v1/topup/crypto", map("kind", "topup", "productKey", productKey, "gb", gb))), CryptoCheckout.class);
    }
}
