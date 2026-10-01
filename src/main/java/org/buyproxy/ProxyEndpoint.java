package org.buyproxy;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** One generated proxy. The API returns {@code username:password@host:port} lines. */
public record ProxyEndpoint(String host, int port, String username, String password, String protocol) {

    public ProxyEndpoint(String host, int port, String username, String password) {
        this(host, port, username, password, "http");
    }

    /** Parse a {@code username:password@host:port} line ('@' and ':' are allowed inside the password). */
    public static ProxyEndpoint parse(String line, String protocol) {
        int at = line.lastIndexOf('@');
        IllegalArgumentException bad = new IllegalArgumentException("Not a proxy line (expected username:password@host:port): \"" + line + "\"");
        if (at < 0) throw bad;
        String credentials = line.substring(0, at);
        String address = line.substring(at + 1);
        int colon = credentials.indexOf(':');
        int lastColon = address.lastIndexOf(':');
        if (colon < 1 || lastColon < 1) throw bad;
        int port;
        try {
            port = Integer.parseInt(address.substring(lastColon + 1));
        } catch (NumberFormatException e) {
            throw bad;
        }
        if (port < 1 || port > 65535) throw bad;
        return new ProxyEndpoint(address.substring(0, lastColon), port, credentials.substring(0, colon), credentials.substring(colon + 1), protocol);
    }

    public static ProxyEndpoint parse(String line) { return parse(line, "http"); }

    /** {@code username:password@host:port} — the form the API uses. */
    @Override public String toString() { return username + ":" + password + "@" + host + ":" + port; }

    /** {@code http://host:port} — no credentials. */
    public String server() { return protocol + "://" + host + ":" + port; }

    /** Full URL with percent-encoded credentials, ready for any HTTP client. */
    public String url() { return protocol + "://" + enc(username) + ":" + enc(password) + "@" + host + ":" + port; }

    /** A {@link java.net.Proxy} for {@code new URL(..).openConnection(proxy)} (HTTP or SOCKS5). */
    public java.net.Proxy toNetProxy() {
        return new java.net.Proxy("socks5".equals(protocol) ? java.net.Proxy.Type.SOCKS : java.net.Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    /**
     * An {@link HttpClient.Builder} already routed through this proxy with its credentials.
     * Note: the JDK disables Basic auth for HTTPS tunnelling by default — start your JVM with
     * {@code -Djdk.http.auth.tunneling.disabledSchemes=""} to use HTTPS targets through an HTTP proxy.
     */
    public HttpClient.Builder httpClientBuilder() {
        return HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress(host, port)))
                .authenticator(new Authenticator() {
                    @Override protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                });
    }

    /** Environment variables understood by curl, wget, git and most CLIs. */
    public Map<String, String> env() {
        Map<String, String> env = new LinkedHashMap<>();
        for (String k : new String[] {"HTTP_PROXY", "HTTPS_PROXY", "ALL_PROXY", "http_proxy", "https_proxy", "all_proxy"}) env.put(k, url());
        return env;
    }

    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20"); }
}
