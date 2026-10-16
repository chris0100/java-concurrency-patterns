package dev.chris0100.concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Factory for executors that never grow memory without limit. */
public final class BoundedExecutors {

    private BoundedExecutors() {
    }

    /**
     * Fixed-size pool with a bounded queue. When the queue is full the caller runs the task,
     * which slows the producer down (back-pressure) instead of throwing or buffering forever.
     */
    public static ThreadPoolExecutor withCallerRuns(String name, int threads, int queueCapacity) {
        return new ThreadPoolExecutor(
                threads, threads,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                namedThreads(name),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    static ThreadFactory namedThreads(String prefix) {
        AtomicInteger seq = new AtomicInteger();
        return r -> {
            Thread t = new Thread(r, prefix + "-" + seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
    }
}
