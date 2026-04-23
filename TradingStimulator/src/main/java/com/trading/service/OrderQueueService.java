package com.trading.service;

import com.trading.model.Stock;
import com.trading.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * MODULE III – Thread and process, Parallel Computing,
 *              Concurrent Programming, Synchronization
 *
 * Demonstrates:
 *  1. Extending Thread directly (PriceBroadcastThread inner class)
 *  2. BlockingQueue for thread-safe producer-consumer pattern
 *  3. synchronized keyword to protect shared mutable state
 *  4. wait/notify coordination between threads
 *
 * In the trading simulator, price updates arrive continuously (producer) and
 * must be broadcast to connected clients (consumer) without race conditions.
 */
@Service
public class OrderQueueService {

    // ── BlockingQueue (Module III — Concurrent Programming) ───────────────────
    // A BlockingQueue is a thread-safe queue — put() blocks if queue is full,
    // take() blocks if queue is empty. No explicit synchronization needed for
    // basic producer-consumer patterns.
    //
    // LinkedBlockingQueue = unbounded (or optionally bounded) FIFO queue backed
    // by linked nodes — better throughput than ArrayBlockingQueue in most cases.
    private final BlockingQueue<String> orderQueue = new LinkedBlockingQueue<>(1000);

    // ── Shared state protected by synchronized ────────────────────────────────
    // This counter is accessed by multiple threads — must be guarded.
    private int processedOrderCount = 0;

    private final StockRepository stockRepository;

    public OrderQueueService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // ── Producer: enqueue an order ID (thread-safe via BlockingQueue) ─────────
    /**
     * Called by HTTP request threads to submit an order for async processing.
     * offer() is non-blocking — returns false if queue is full instead of throwing.
     */
    public boolean submitOrder(String orderId) {
        boolean accepted = orderQueue.offer(orderId);
        if (!accepted) {
            System.err.println("[OrderQueue] Queue full! Order rejected: " + orderId);
        }
        return accepted;
    }

    // ── Consumer: process next order (blocking take) ──────────────────────────
    /**
     * Retrieves and removes the next order from the queue.
     * Blocks (waits) if the queue is empty — no busy-waiting.
     * Throws InterruptedException if the thread is interrupted while waiting.
     */
    public String consumeNextOrder() throws InterruptedException {
        return orderQueue.take(); // Blocks until an order is available
    }

    // ── synchronized method (Module III) ─────────────────────────────────────
    /**
     * Increments the processed order counter.
     * 'synchronized' ensures only ONE thread executes this method at a time
     * on the same object instance — prevents race conditions.
     */
    public synchronized void incrementProcessedCount() {
        processedOrderCount++; // Safe: only one thread enters this at a time
    }

    public synchronized int getProcessedOrderCount() {
        return processedOrderCount; // Consistent read
    }

    /** Returns current queue depth — useful for monitoring */
    public int getQueueDepth() {
        return orderQueue.size();
    }

    // ── Static nested Thread class (Module III — Thread and process) ──────────
    /**
     * PriceBroadcastThread extends Thread — the simplest way to create a thread.
     *
     * Two ways to create threads in Java:
     *  1. extends Thread       — override run()     ← demonstrated here
     *  2. implements Runnable  — pass to new Thread ← used in MarketSimulator
     *
     * Thread concepts demonstrated:
     *  - Thread lifecycle: NEW → RUNNABLE → RUNNING → TERMINATED
     *  - Thread.sleep() for controlled pausing (simulates latency)
     *  - Daemon thread: runs in the background, JVM exits even if it's still running
     */
    public static class PriceBroadcastThread extends Thread {

        private final List<Stock> stocks;
        private volatile boolean running = true; // volatile ensures visibility across threads

        public PriceBroadcastThread(List<Stock> stocks) {
            this.stocks = stocks;
            // ── Daemon thread (Module III) ────────────────────────────────
            // Daemon threads do not prevent JVM shutdown.
            // Non-daemon (user) threads DO prevent JVM from exiting.
            this.setDaemon(true);
            this.setName("PriceBroadcastThread");
        }

        // ── run() is the thread entry point ──────────────────────────────────
        // This code executes in a separate thread when start() is called.
        @Override
        public void run() {
            System.out.println("[Thread] " + getName() + " started. TID=" + getId());

            // ── while loop with volatile flag for safe termination ────────────
            while (running) {
                try {
                    // ── Thread.sleep (Module III — Thread control) ────────────
                    // Pauses this thread for 500ms without consuming CPU.
                    // InterruptedException is thrown if another thread calls interrupt().
                    Thread.sleep(500);

                    // Simulate broadcasting prices
                    for (Stock s : stocks) {
                        System.out.printf("[BROADCAST] %s = ₹%.2f%n",
                            s.getSymbol(), s.getCurrentPrice());
                    }

                } catch (InterruptedException e) {
                    // Restore the interrupt flag and exit gracefully
                    Thread.currentThread().interrupt();
                    System.out.println("[Thread] " + getName() + " interrupted — stopping.");
                    break;
                }
            }

            System.out.println("[Thread] " + getName() + " terminated.");
        }

        /** Signals the thread to stop on its next loop check */
        public void stopBroadcast() {
            this.running = false;
            this.interrupt(); // Wake up from sleep() immediately
        }
    }
}
