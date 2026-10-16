package dev.chris0100.concurrency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/** Lock-free counters per endpoint. */
public final class RequestStats {

    private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();

    public void record(String endpoint) {
        counters.computeIfAbsent(endpoint, k -> new LongAdder()).increment();
    }

    public long count(String endpoint) {
        LongAdder adder = counters.get(endpoint);
        return adder == null ? 0 : adder.sum();
    }

    public Map<String, Long> snapshot() {
        Map<String, Long> copy = new java.util.TreeMap<>();
        counters.forEach((k, v) -> copy.put(k, v.sum()));
        return copy;
    }
}
