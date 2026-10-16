# Java Concurrency Patterns

Small, runnable examples of the concurrency patterns I use most in backend services, each one with a JUnit test that proves the behavior.

| Pattern | Class | What it shows |
|---------|-------|---------------|
| Bounded executor with back-pressure | `BoundedExecutors` | Fixed pool + bounded queue + `CallerRunsPolicy`, so a burst slows the producer instead of growing memory without limit |
| Per-key locking | `KeyedLock` | Serialize work for the same key (e.g. the same order id) while different keys run in parallel |
| Fan-out / fan-in with timeout | `ParallelAggregator` | `CompletableFuture` calls in parallel, a global timeout and a fallback value per call |
| Thread-safe counters | `RequestStats` | `LongAdder` + `ConcurrentHashMap.merge` instead of `synchronized` |

## Run

```bash
mvn test
```

## Key ideas (interview notes)

- A `ThreadPoolExecutor` only grows beyond the core size when the queue is **full**. With an unbounded queue, `maxPoolSize` is never used.
- `CallerRunsPolicy` gives natural back-pressure: the thread that submits the task runs it, so it cannot submit more until it finishes.
- Never do blocking I/O inside `ConcurrentHashMap.computeIfAbsent`; the bin stays locked while the function runs.
- `LongAdder` beats `AtomicLong` under high contention because it spreads updates across cells.
- Always give `CompletableFuture` a timeout (`orTimeout` / `completeOnTimeout`) and your own executor for blocking calls, not the common pool.
