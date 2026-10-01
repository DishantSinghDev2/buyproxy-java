package org.buyproxy;

import java.util.Iterator;
import java.util.List;

/** The result of a generate call: raw lines plus parsed proxies. */
public record GeneratedProxies(List<String> lines, List<ProxyEndpoint> proxies) implements Iterable<ProxyEndpoint> {

    static GeneratedProxies of(List<String> lines, String protocol) {
        return new GeneratedProxies(List.copyOf(lines), lines.stream().map(l -> ProxyEndpoint.parse(l, protocol)).toList());
    }

    public int size() { return proxies.size(); }

    public ProxyEndpoint get(int index) { return proxies.get(index); }

    @Override public Iterator<ProxyEndpoint> iterator() { return proxies.iterator(); }
}
