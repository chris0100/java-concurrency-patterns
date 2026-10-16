package dev.chris0100.concurrency;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class RequestStatsTest {

    @Test
    void countsAreExactUnderParallelLoad() {
        RequestStats stats = new RequestStats();
        IntStream.range(0, 100_000).parallel().forEach(i -> stats.record(i % 2 == 0 ? "/orders" : "/users"));

        assertEquals(50_000L, stats.count("/orders"));
        assertEquals(50_000L, stats.count("/users"));
        assertEquals(0L, stats.count("/missing"));
    }
}
