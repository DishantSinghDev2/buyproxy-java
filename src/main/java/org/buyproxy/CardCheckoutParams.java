package org.buyproxy;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/** A card checkout request: a pay-as-you-go top-up or a plan subscription. */
public final class CardCheckoutParams {
    private final Map<String, Object> body = new LinkedHashMap<>();

    private CardCheckoutParams() {}

    /** Buy {@code gb} GB of one product (see {@link BuyProxy#products()} for keys and minimums). */
    public static CardCheckoutParams topup(String productKey, int gb) {
        CardCheckoutParams p = new CardCheckoutParams();
        p.body.put("kind", "topup");
        p.body.put("productKey", productKey);
        p.body.put("gb", gb);
        return p;
    }

    /** Subscribe to {@code pro}, {@code business} or {@code advanced}. {@code interval} is {@code "month"} (default) or {@code "year"}. */
    public static CardCheckoutParams plan(String plan, @Nullable String interval) {
        CardCheckoutParams p = new CardCheckoutParams();
        p.body.put("kind", "plan");
        p.body.put("plan", plan);
        if (interval != null) p.body.put("interval", interval);
        return p;
    }

    Map<String, Object> toBody() { return body; }
}
