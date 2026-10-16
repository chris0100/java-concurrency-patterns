package dev.chris0100.concurrency;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class KeyedLockTest {

    private int unsafeCounter; // deliberately not thread-safe: the lock must protect it

    @Test
    void sameKeyIsSerializedAndLocksAreCleanedUp() throws Exception {
        KeyedLock<String> lock = new KeyedLock<>();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 1_000; i++) {
            futures.add(pool.submit(() -> lock.withLock("order-42", () -> unsafeCounter++)));
        }
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        assertEquals(1_000, unsafeCounter);
        assertEquals(0, lock.activeKeys());
    }
}
