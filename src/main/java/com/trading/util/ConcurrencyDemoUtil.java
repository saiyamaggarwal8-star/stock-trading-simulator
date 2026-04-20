package com.trading.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.*;

/**
 * MODULE IV – Concurrency in Java (dedicated class)
 * MODULE III – Parallel Computing, Concurrent Programming, Synchronization
 *
 * This class is a dedicated, focused demonstration of Java's java.util.concurrent
 * package — the advanced concurrency toolkit introduced in Java 5+.
 *
 * It complements OrderQueueService (which shows synchronized + BlockingQueue)
 * by covering the remaining concurrency tools from the syllabus:
 *
 *  Tool                   | What it solves
 *  ─────────────────────────────────────────────────────────────────────────
 *  ExecutorService        | Thread Pool management — reuse threads instead of
 *                         | creating a new thread per task (expensive!)
 *  Callable + Future      | Like Runnable but CAN return a value and throw exceptions
 *  AtomicInteger/Long     | Lock-free thread-safe counters (CAS operations)
 *  ReentrantLock          | Explicit lock — more flexible than synchronized
 *  ReadWriteLock          | Multiple readers OR one writer (optimised for reads)
 *  ConcurrentHashMap      | Thread-safe hash map (better than Collections.synchronizedMap)
 *  CountDownLatch         | Wait for N threads to finish before proceeding
 *  Semaphore              | Limit concurrent access to a resource (e.g., rate limiting)
 *  ─────────────────────────────────────────────────────────────────────────
 *
 * In the trading simulator context, all of these patterns apply to scenarios
 * such as parallel price fetching, rate-limited API calls, and concurrent
 * order book updates.
 */
public class ConcurrencyDemoUtil {

    // ── AtomicInteger (Module IV — Concurrency) ───────────────────────────────
    // Atomic types use Compare-And-Swap (CAS) CPU instructions — NO locks needed.
    // Safe to increment/decrement from many threads simultaneously.
    private static final AtomicInteger totalOrdersReceived  = new AtomicInteger(0);
    private static final AtomicLong    totalTradedValuePaise = new AtomicLong(0L);

    // ── ReentrantLock (Module IV) ─────────────────────────────────────────────
    // Explicit lock — unlike synchronized, it supports:
    //  - tryLock()         : try to acquire without blocking
    //  - lockInterruptibly : acquire unless the thread is interrupted
    //  - Fairness policy   : optional FIFO ordering
    private static final Lock orderBookLock = new ReentrantLock(true); // fair=true

    // ── ReadWriteLock (Module IV) ─────────────────────────────────────────────
    // Multiple threads CAN read simultaneously; write requires exclusive access.
    // Ideal for a price cache where reads greatly outnumber writes.
    private static final ReadWriteLock priceTableLock = new ReentrantReadWriteLock();
    private static final Map<String, Double> priceTable = new HashMap<>(); // guarded by priceTableLock

    // ── ConcurrentHashMap (Module IV) ────────────────────────────────────────
    // Thread-safe map with segment-level locking — much faster than Hashtable
    // or Collections.synchronizedMap() under concurrent load.
    // Used here as an in-memory order-count register per stock symbol.
    private static final ConcurrentHashMap<String, AtomicInteger> orderCountBySymbol
            = new ConcurrentHashMap<>();

    // ─────────────────────────────────────────────────────────────────────────
    // 1. ExecutorService — Thread Pool (Module IV Concurrency)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Demonstrates a Fixed Thread Pool.
     *
     * Problem without pools: creating a new Thread for every order is EXPENSIVE
     * (OS-level resource creation, stack allocation). A pool reuses worker threads.
     *
     * Executors.newFixedThreadPool(n) → always n threads alive, tasks queue up if busy.
     */
    public static void demonstrateThreadPool() throws InterruptedException {
        // Create a pool of 4 worker threads (simulating 4 CPU cores)
        ExecutorService pool = Executors.newFixedThreadPool(4);

        System.out.println("[ConcurrencyDemo] Submitting 10 order-processing tasks to pool...");

        // Submit 10 tasks — pool queues extras until a thread is free
        for (int i = 1; i <= 10; i++) {
            final int orderId = i;
            pool.submit(() -> {
                // Runnable lambda — Module IV (Lambda expression)
                String threadName = Thread.currentThread().getName();
                System.out.printf("[Pool] Worker '%s' processing order #%d%n", threadName, orderId);
                totalOrdersReceived.incrementAndGet(); // AtomicInteger — thread-safe ++
            });
        }

        // Shutdown — no new tasks accepted; existing tasks complete
        pool.shutdown();
        // awaitTermination blocks until all tasks finish or timeout
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("[ConcurrencyDemo] Pool done. Total orders: " + totalOrdersReceived.get());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Callable + Future — return value from a thread (Module IV)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Callable<T> is like Runnable but:
     *  - Has a generic return type T
     *  - Can throw checked exceptions
     *
     * Future<T> holds the result — get() blocks until the thread finishes.
     * Used here to fetch prices in parallel and gather results.
     */
    public static double fetchPriceWithFuture(String symbol) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Callable lambda returns a double (simulated price fetch)
        Callable<Double> priceFetcher = () -> {
            Thread.sleep(100); // Simulate network delay
            // In the real app this calls Yahoo Finance; here we simulate
            return 1000.0 + (symbol.hashCode() % 500);
        };

        // submit() returns a Future that wraps the eventual result
        Future<Double> future = executor.submit(priceFetcher);

        // future.get() blocks here until the Callable finishes
        double price = future.get(3, TimeUnit.SECONDS); // Timeout after 3 s

        executor.shutdown();
        System.out.printf("[Future] Fetched %s = ₹%.2f%n", symbol, price);
        return price;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. ReentrantLock — explicit locking for order book updates
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates the order book record for a symbol.
     * Uses ReentrantLock for explicit locking — supports tryLock() unlike synchronized.
     */
    public static boolean recordOrderInBook(String symbol, int quantity) {
        // tryLock() returns false immediately if another thread holds the lock
        // This avoids blocking the caller indefinitely (unlike synchronized)
        if (orderBookLock.tryLock()) {
            try {
                // Critical section — only one thread executes this at a time
                orderCountBySymbol
                    .computeIfAbsent(symbol, k -> new AtomicInteger(0))
                    .addAndGet(quantity);
                System.out.println("[Lock] Order book updated: " + symbol + " +" + quantity);
                return true;
            } finally {
                // ALWAYS release in finally — otherwise deadlock if exception occurs
                orderBookLock.unlock();
            }
        } else {
            System.out.println("[Lock] Order book lock busy, order rejected: " + symbol);
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. ReadWriteLock — concurrent reads, exclusive writes on price cache
    // ─────────────────────────────────────────────────────────────────────────

    /** Write price — acquires EXCLUSIVE write lock (blocks all readers) */
    public static void updatePriceTable(String symbol, double price) {
        Lock writeLock = priceTableLock.writeLock();
        writeLock.lock();
        try {
            priceTable.put(symbol, price); // Safe: no concurrent readers
        } finally {
            writeLock.unlock();
        }
    }

    /** Read price — acquires SHARED read lock (allows other concurrent readers) */
    public static double readPriceTable(String symbol) {
        Lock readLock = priceTableLock.readLock();
        readLock.lock(); // Multiple threads can hold this simultaneously
        try {
            return priceTable.getOrDefault(symbol, -1.0);
        } finally {
            readLock.unlock();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. CountDownLatch — wait for N parallel price fetchers before continuing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Simulates parallel price refresh for multiple symbols.
     * CountDownLatch(n) starts at n; each thread calls countDown() when done.
     * Main thread calls await() and blocks until latch reaches 0.
     */
    public static void parallelPriceRefresh(List<String> symbols) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(symbols.size());
        ExecutorService pool = Executors.newCachedThreadPool();

        for (String symbol : symbols) {
            pool.submit(() -> {
                try {
                    // Simulate fetching price for one symbol
                    double price = 500.0 + (symbol.hashCode() % 1500);
                    updatePriceTable(symbol, price);
                    System.out.printf("[Latch] Refreshed %s = ₹%.2f%n", symbol, price);
                } finally {
                    latch.countDown(); // Signal this task is complete
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS); // Block until all symbols refreshed
        pool.shutdown();
        System.out.println("[Latch] All " + symbols.size() + " prices refreshed.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Semaphore — rate-limit concurrent Yahoo Finance API calls
    // ─────────────────────────────────────────────────────────────────────────

    // Allow at most 3 simultaneous Yahoo Finance API calls (rate limit compliance)
    private static final Semaphore yahooApiSemaphore = new Semaphore(3);

    /**
     * Semaphore controls how many threads can access a resource concurrently.
     * acquire() blocks if the permit count is already at max; release() frees a permit.
     *
     * Analogy: 3 checkout counters at a store — the 4th customer waits.
     */
    public static void fetchWithRateLimit(String symbol) {
        try {
            yahooApiSemaphore.acquire(); // Blocks if 3 calls are already in progress
            try {
                System.out.println("[Semaphore] Calling Yahoo Finance for: " + symbol
                        + " | Permits remaining: " + yahooApiSemaphore.availablePermits());
                Thread.sleep(200); // Simulate API call latency
            } finally {
                yahooApiSemaphore.release(); // ALWAYS release, even if an exception occurs
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Semaphore] Thread interrupted for: " + symbol);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. Atomic operations — lock-free thread-safe counter
    // ─────────────────────────────────────────────────────────────────────────

    /** Adds to total traded value in paise (integer math avoids floating-point issues) */
    public static void addTradedValue(long amountInPaise) {
        // addAndGet is atomic — no lock needed
        totalTradedValuePaise.addAndGet(amountInPaise);
    }

    public static double getTotalTradedValueRupees() {
        // Unboxing: AtomicLong.get() returns primitive long (Module I)
        return totalTradedValuePaise.get() / 100.0;
    }

    public static int getTotalOrdersReceived() {
        return totalOrdersReceived.get(); // AtomicInteger read — always consistent
    }
}
