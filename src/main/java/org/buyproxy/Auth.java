package org.buyproxy;

import org.buyproxy.BuyProxy.Req;
import org.buyproxy.model.*;
import org.jspecify.annotations.Nullable;

import static org.buyproxy.BuyProxy.map;

/** Create an account without a browser: {@code signup}, then {@code verifyOtp} with the emailed code. */
public final class Auth extends Resource {
    Auth(BuyProxy c) { super(c); }

    public SignupResult signup(String email, String password, @Nullable String name) {
        return c.read(c.send(Req.post("/api/auth/signup", map("email", email, "password", password, "name", name)).asPublic()), SignupResult.class);
    }

    public VerifyResult verifyOtp(String email, String code) {
        return c.read(c.send(Req.post("/api/auth/verify-otp", map("email", email, "code", code)).asPublic()), VerifyResult.class);
    }

    public void resendOtp(String email) { c.send(Req.post("/api/auth/resend-otp", map("email", email)).asPublic()); }
}
