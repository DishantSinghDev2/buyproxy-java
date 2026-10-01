# BuyProxy Java SDK — Residential Proxy API Client for the JVM

[![Maven Central](https://img.shields.io/maven-central/v/org.buyproxy/buyproxy?logo=apachemaven&logoColor=white)](https://central.sonatype.com/artifact/org.buyproxy/buyproxy)
[![CI](https://github.com/DishantSinghDev2/buyproxy-java/actions/workflows/ci.yml/badge.svg)](https://github.com/DishantSinghDev2/buyproxy-java/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17%2B-orange)
[![license](https://img.shields.io/badge/license-MIT-green)](LICENSE)

The official **Java SDK** for [BuyProxy](https://www.buyproxy.org): generate **residential, mobile, ISP and datacenter proxies**, check bandwidth, top up by card or crypto, and manage **reseller** customers. Java 17+, immutable records, built on the JDK's `HttpClient` — works with **Spring**, **Quarkus**, **Micronaut** and plain Java.

**Keywords:** residential proxies · rotating proxies · mobile proxies · ISP proxies · datacenter proxies · SOCKS5 · proxy API · web scraping · proxy reseller

## Features

- Immutable `record` models, `@Nullable`-annotated for NullAway/Kotlin
- Unchecked, typed exceptions — compose cleanly with streams and lambdas
- Automatic retries with back-off for rate limits and transient errors
- `ProxyEndpoint.httpClientBuilder()` returns a `java.net.http.HttpClient` already routed through the proxy

## Installation

**Maven**

```xml
<dependency>
  <groupId>org.buyproxy</groupId>
  <artifactId>buyproxy</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Gradle**

```kotlin
implementation("org.buyproxy:buyproxy:1.0.0")
```

Java 17+. Depends only on Jackson (`jackson-databind`) and `jspecify` annotations.

## Quick start

```java
import org.buyproxy.*;

BuyProxy bp = BuyProxy.builder().apiKey(System.getenv("BUYPROXY_API_KEY")).build(); // or omit — reads BUYPROXY_API_KEY

var plan = bp.plans().list().get(0);
GeneratedProxies result = bp.plans().generate(plan.id(),
        GenerateParams.create().country("US").city("Austin").count(5));

System.out.println(result.get(0).url()); // http://user-country-us-city-austin:pass@gw.buyproxy.org:6969
```

Add `.stickyMinutes(10)` to keep the same IP for ten minutes, or `.protocol("socks5")` for SOCKS5.

## Using the proxies with your stack

### java.net.http.HttpClient

```java
HttpClient http = result.get(0).httpClientBuilder().build();
String ip = http.send(HttpRequest.newBuilder(URI.create("http://api.ipify.org")).build(), BodyHandlers.ofString()).body();
```

> The JDK disables Basic auth for HTTPS tunnelling by default. To reach `https://` targets through an HTTP proxy, start the JVM with `-Djdk.http.auth.tunneling.disabledSchemes=""`.

### HttpURLConnection

```java
HttpURLConnection conn = (HttpURLConnection) new URL("http://api.ipify.org").openConnection(result.get(0).toNetProxy());
```

### Spring Boot

```java
@Configuration
class BuyProxyConfig {
    @Bean
    BuyProxy buyProxy(@Value("${buyproxy.api-key}") String key) {
        return BuyProxy.builder().apiKey(key).build();
    }
}
```

### Selenium / Playwright

```java
// Selenium: new org.openqa.selenium.Proxy().setHttpProxy(p.host() + ":" + p.port())
// Playwright: new Browser.NewContextOptions().setProxy(new Proxy(p.server()).setUsername(p.username()).setPassword(p.password()))
```

## Buy bandwidth (card & crypto)

```java
// Buy 10 GB of Core Residential by card — send the customer to payUrl
CardCheckout checkout = bp.topup().card(CardCheckoutParams.topup("residential", 10));
System.out.println(checkout.payUrl());

// Or subscribe to a plan (monthly or yearly)
bp.topup().card(CardCheckoutParams.plan("pro", "year"));

// Crypto (pay-as-you-go top-ups only)
bp.topup().crypto("residential", 50);

// Wait for the payment to settle — GB appears on bp.balances().list() right after
Transaction tx = bp.transactions().await(checkout.transactionId(), Duration.ofSeconds(3), Duration.ofMinutes(10));
```

Card payments run through Stripe Checkout; crypto is available for pay-as-you-go top-ups.

## Reseller API

Build a proxy product on top of BuyProxy: buy bandwidth in bulk into a **reseller pool**, create isolated **customers**, and allocate GB to them. Needs a `bp_live_` key on a Business plan or higher.

```java
BuyProxy reseller = BuyProxy.builder().apiKey("bp_live_…").build();

reseller.reseller().pool().topupCard("residential", 500);                 // buy into your pool
Customer customer = reseller.reseller().customers().create("Acme Inc");   // isolated sub-account

reseller.reseller().customers().allocate(customer.username(), "residential", 20, "order-1042"); // idempotency key → safe to retry

var detail = reseller.reseller().customers().get(customer.username());
GeneratedProxies result = reseller.reseller().customers()
        .generate(customer.username(), detail.plans().get(0).id(), GenerateParams.create().country("DE"));

reseller.reseller().customers().update(customer.username(), null, "suspended");
```

## Error handling

```java
try {
    bp.plans().generate(planId);
} catch (BuyProxyException.InsufficientBandwidth e) {
    System.out.println("Out of GB — top up first");
} catch (BuyProxyException.RateLimit e) {
    System.out.println("Slow down; retry in " + e.retryAfterSeconds() + "s");
} catch (BuyProxyException.Authentication e) {
    System.out.println("Check your API key");
}
```
Every exception extends `BuyProxyException` and exposes `status()` and `body()`.

## Configuration

```java
BuyProxy.builder()
    .apiKey("bp_key_…")                        // default: $BUYPROXY_API_KEY
    .baseUrl("https://www.buyproxy.org")
    .timeout(Duration.ofSeconds(30))
    .maxRetries(2)                              // 429s always; 5xx only for safe requests
    .httpClient(myHttpClient)                   // bring your own java.net.http.HttpClient
    .build();
```

## API overview

| Capability | Call |
|---|---|
| Catalog & prices | `bp.products().list()` |
| Account | `bp.account().get()` |
| Spendable GB per product | `bp.balances().list()` |
| Plans | `bp.plans().list()` |
| Generate proxies | `bp.plans().generate(planId, params)` |
| Card checkout | `bp.topup().card(params)` |
| Crypto checkout | `bp.topup().crypto(product, gb)` |
| Payments | `bp.transactions().list/get/await` |
| Create an account | `bp.auth().signup/verifyOtp/resendOtp` |
| Reseller summary | `bp.reseller().me()` |
| Customers | `bp.reseller().customers().list/create/get/update/usage/allocate/generate` |
| Reseller pool | `bp.reseller().pool().get/topupCard/topupCrypto` |

Full endpoint reference: <https://www.buyproxy.org/docs/api-reference/introduction> · OpenAPI spec: <https://www.buyproxy.org/openapi.yaml>

## FAQ

**How do I get an API key?**
Create a free account at [buyproxy.org](https://www.buyproxy.org/signup), then open **Dashboard → API keys**. Personal keys start with `bp_key_`; reseller keys (Business plan and above) start with `bp_live_`. You can create the *account* from code (`signup` + `verify otp`), but keys are issued from the dashboard.

**What's the difference between a personal and a reseller key?**
A personal key (`bp_key_…`) manages your own bandwidth: balances, plans, generating proxies, top-ups and payments. A reseller key (`bp_live_…`) manages *your customers*: create isolated sub-accounts, buy bandwidth into a pool, allocate it, suspend or reactivate, and generate proxies on their behalf.

**Which proxy types are available?**
Core Residential, Premium Residential, ISP Rotating, Mobile, Datacenter IPv4 and IPv6 — call the catalog endpoint for the live list and per-GB prices (from **$1.99/GB**).

**Do generated proxies expire, and when is bandwidth used?**
Generating a proxy is free and instant; bandwidth is only consumed when traffic flows through it. Lines are on-demand — generate fresh ones rather than storing them.

**What are the rate limits?**
60 requests per minute per API key. The SDK honours `Retry-After` and retries rate-limited requests automatically.

**Is it free to use the SDK?**
Yes — the SDK is MIT-licensed. You pay only for the proxy bandwidth you buy.

## Other BuyProxy SDKs

[Node.js](https://github.com/DishantSinghDev2/buyproxy-node) · [Python](https://github.com/DishantSinghDev2/buyproxy-python) · [Go](https://github.com/DishantSinghDev2/buyproxy-go) · [PHP](https://github.com/DishantSinghDev2/buyproxy-php) · [Ruby](https://github.com/DishantSinghDev2/buyproxy-ruby) · [Java](https://github.com/DishantSinghDev2/buyproxy-java) · [.NET](https://github.com/DishantSinghDev2/buyproxy-dotnet) · [Rust](https://github.com/DishantSinghDev2/buyproxy-rust) · [CLI](https://github.com/DishantSinghDev2/buyproxy-cli)

All SDKs are checked against the same request log, so behaviour is identical across languages — see [buyproxy-sdks](https://github.com/DishantSinghDev2/buyproxy-sdks).

## Development

```bash
mvn verify
```

Issues and pull requests are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md). Security reports: [SECURITY.md](SECURITY.md).

## Links

- Website: <https://www.buyproxy.org>
- Documentation: <https://www.buyproxy.org/docs/sdks>
- Pricing: <https://www.buyproxy.org/pricing>
- Support: <support@buyproxy.org>

## License

[MIT](LICENSE) © BuyProxy
