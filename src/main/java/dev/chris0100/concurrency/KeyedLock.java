package dev.chris0100.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Runs actions one at a time per key, while different keys run in parallel.
 * Locks are reference-counted and removed when nobody uses them, so the map does not leak.
 */
public final class KeyedLock<K> {

    private static final class Entry {
        final ReentrantLock lock = new ReentrantLock();
        int users;
    }

    private final ConcurrentHashMap<K, Entry> locks = new ConcurrentHashMap<>();

    public <T> T withLock(K key, Supplier<T> action) {
        Entry entry = locks.compute(key, (k, e) -> {
            Entry current = (e == null) ? new Entry() : e;
            current.users++;
            return current;
        });
        entry.lock.lock();
        try {
            return action.get();
        } finally {
            entry.lock.unlock();
            locks.computeIfPresent(key, (k, e) -> --e.users == 0 ? null : e);
        }
    }

    /** Number of keys currently holding a lock entry (useful to prove there is no leak). */
    public int activeKeys() {
        return locks.size();
    }
}
