package dev.chris0100.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Calls several slow dependencies in parallel; each one falls back to a default on timeout or error. */
public final class ParallelAggregator {

    private final Executor executor;

    public ParallelAggregator(Executor executor) {
        this.executor = executor;
    }

    public <T> List<T> callAll(List<Supplier<T>> calls, Duration timeout, T fallback) {
        List<CompletableFuture<T>> futures = calls.stream()
                .map(call -> CompletableFuture.supplyAsync(call, executor)
                        .completeOnTimeout(fallback, timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .exceptionally(ex -> fallback))
                .toList();
        return futures.stream().map(CompletableFuture::join).toList();
    }
}
