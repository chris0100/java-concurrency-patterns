package dev.chris0100.concurrency;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ParallelAggregatorTest {

    @Test
    void slowAndFailingCallsFallBackWithoutBlockingTheOthers() {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        ParallelAggregator aggregator = new ParallelAggregator(pool);

        Supplier<String> fast = () -> "fast";
        Supplier<String> slow = () -> {
            sleep(2_000);
            return "slow";
        };
        Supplier<String> failing = () -> {
            throw new IllegalStateException("downstream 500");
        };

        long start = System.nanoTime();
        List<String> result = aggregator.callAll(List.of(fast, slow, failing), Duration.ofMillis(200), "fallback");
        long tookMs = (System.nanoTime() - start) / 1_000_000;

        assertEquals(List.of("fast", "fallback", "fallback"), result);
        assertTrue(tookMs < 1_500, "took " + tookMs + " ms");
        pool.shutdownNow();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
