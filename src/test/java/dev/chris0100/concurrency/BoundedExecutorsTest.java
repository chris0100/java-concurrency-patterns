package dev.chris0100.concurrency;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BoundedExecutorsTest {

    @Test
    void whenQueueIsFullTheCallerRunsTheTask() throws Exception {
        ThreadPoolExecutor pool = BoundedExecutors.withCallerRuns("worker", 1, 1);
        CountDownLatch release = new CountDownLatch(1);
        Set<String> threadsUsed = ConcurrentHashMap.newKeySet();

        Runnable blocking = () -> {
            threadsUsed.add(Thread.currentThread().getName());
            try {
                release.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        pool.execute(blocking);        // occupies the only worker
        pool.execute(blocking);        // fills the queue (capacity 1)
        pool.execute(() -> threadsUsed.add(Thread.currentThread().getName())); // rejected -> caller runs

        assertTrue(threadsUsed.contains(Thread.currentThread().getName()));
        release.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));
    }
}
