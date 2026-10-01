package org.buyproxy;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/** Controls proxy generation. Unset fields use the API's defaults. */
public final class GenerateParams {
    private @Nullable String protocol;
    private @Nullable Integer count;
    private @Nullable String country;
    private @Nullable String state;
    private @Nullable String city;
    private @Nullable String isp;
    private @Nullable Integer stickyMinutes;

    private GenerateParams() {}

    public static GenerateParams create() { return new GenerateParams(); }

    /** {@code "http"} (default) or {@code "socks5"}. */
    public GenerateParams protocol(String protocol) { this.protocol = protocol; return this; }
    /** 1–1000, default 1. */
    public GenerateParams count(int count) { this.count = count; return this; }
    /** ISO 3166-1 alpha-2 country code, e.g. {@code "US"}. */
    public GenerateParams country(String country) { this.country = country; return this; }
    public GenerateParams state(String state) { this.state = state; return this; }
    public GenerateParams city(String city) { this.city = city; return this; }
    public GenerateParams isp(String isp) { this.isp = isp; return this; }
    /** Keep the same IP this long; leave unset for a fresh IP per request (rotating). */
    public GenerateParams stickyMinutes(int minutes) { this.stickyMinutes = minutes; return this; }

    @Nullable String protocolOrNull() { return protocol; }

    Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        if (protocol != null) body.put("protocol", protocol);
        if (count != null) body.put("count", count);
        if (country != null) body.put("country", country);
        if (state != null) body.put("state", state);
        if (city != null) body.put("city", city);
        if (isp != null) body.put("isp", isp);
        if (stickyMinutes != null) body.put("stickyMinutes", stickyMinutes);
        return body;
    }
}
