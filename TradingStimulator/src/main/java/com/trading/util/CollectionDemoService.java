package com.trading.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * MODULE II – ArrayList, LinkedList, Vector collections
 * MODULE IV – Collection framework: HashMap, LinkedHashMap, TreeMap
 *             Concurrency in Java (Vector as thread-safe list)
 *
 * Demonstrates all major Collection framework classes in the context of
 * an in-memory order book and price cache for the trading simulator.
 *
 * When to use which?
 * ───────────────────────────────────────────────────────────────────────────
 * ArrayList     – Fast random access (get by index), grows dynamically
 * LinkedList    – Fast insertion/removal at ends (deque/queue use cases)
 * Vector        – Like ArrayList but synchronized (thread-safe, legacy)
 * HashMap       – Key→Value, O(1) lookup, no ordering guarantee
 * LinkedHashMap – Key→Value, preserves INSERTION order (ledger / audit log)
 * TreeMap       – Key→Value, keys kept in SORTED (natural) order
 * ───────────────────────────────────────────────────────────────────────────
 */
@Component
public class CollectionDemoService {

    // ── ArrayList (Module II) ─────────────────────────────────────────────────
    // Random-access ordered list.  Fast get(i), slow insert/remove at middle.
    private final ArrayList<String> pendingOrderIds = new ArrayList<>();

    // ── LinkedList (Module II) ────────────────────────────────────────────────
    // Doubly-linked list.  Fast add/remove at head and tail — ideal for a queue.
    private final LinkedList<String> executionQueue = new LinkedList<>();

    // ── Vector (Module II / Module IV thread-safety) ──────────────────────────
    // Synchronized version of ArrayList — every method is thread-safe.
    // Use only when multiple threads must access the same list without explicit locking.
    private final Vector<String> broadcastLog = new Vector<>();

    // ── HashMap (Module IV) ───────────────────────────────────────────────────
    // Key-Value pairs with O(1) average get/put.  NO ordering guarantee.
    // Ideal for a price cache: stockSymbol → latestPrice.
    private final HashMap<String, Double> priceCache = new HashMap<>();

    // ── LinkedHashMap (Module IV) ─────────────────────────────────────────────
    // Maintains insertion order.  Perfect for an audit ledger where you need
    // to print events in the order they occurred.
    private final LinkedHashMap<String, String> tradeAuditLedger = new LinkedHashMap<>();

    // ── TreeMap (Module IV) ───────────────────────────────────────────────────
    // Backed by a Red-Black Tree — keys are always in natural sorted order.
    // Great for a sorted portfolio view (alphabetically by symbol).
    private final TreeMap<String, Double> sortedPortfolio = new TreeMap<>();

    // ─────────────────────────────────────────────────────────────────────────
    // ArrayList operations
    // ─────────────────────────────────────────────────────────────────────────

    /** Adds an order ID to the pending list (ArrayList) */
    public void addPendingOrder(String orderId) {
        pendingOrderIds.add(orderId);
    }

    /** Gets pending order by index — O(1), ArrayList's strength */
    public String getPendingOrder(int index) {
        return pendingOrderIds.get(index);               // Fast random access
    }

    /** Returns a copy of all pending order IDs */
    public List<String> getAllPendingOrders() {
        return Collections.unmodifiableList(pendingOrderIds); // Read-only view
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LinkedList as a Queue (FIFO: First In, First Out)
    // ─────────────────────────────────────────────────────────────────────────

    /** Enqueues an order for execution (add to tail) */
    public void enqueueOrder(String orderId) {
        executionQueue.addLast(orderId);   // O(1) add at tail
    }

    /** Dequeues the next order for execution (remove from head) */
    public String dequeueOrder() {
        return executionQueue.isEmpty() ? null : executionQueue.removeFirst(); // O(1) remove from head
    }

    public int executionQueueSize() {
        return executionQueue.size();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Vector as thread-safe broadcast log
    // ─────────────────────────────────────────────────────────────────────────

    /** Thread-safe append to broadcast log (Vector is synchronized) */
    public synchronized void logBroadcast(String message) {
        broadcastLog.add(message);         // Every Vector method is internally synchronized
    }

    public int broadcastCount() {
        return broadcastLog.size();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HashMap — price cache (no ordering)
    // ─────────────────────────────────────────────────────────────────────────

    /** Updates the latest price for a symbol in the cache */
    public void updatePrice(String symbol, double price) {
        priceCache.put(symbol, price);       // O(1) insert/update
    }

    /** Retrieves the cached price for a symbol, or -1 if absent */
    public double getCachedPrice(String symbol) {
        return priceCache.getOrDefault(symbol, -1.0); // Default-value shorthand
    }

    /** Checks if a price is cached */
    public boolean isPriceCached(String symbol) {
        return priceCache.containsKey(symbol); // O(1) key lookup
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LinkedHashMap — insertion-ordered audit ledger
    // ─────────────────────────────────────────────────────────────────────────

    /** Records a trade event in insertion order */
    public void recordAuditEntry(String tradeId, String description) {
        tradeAuditLedger.put(tradeId, description);  // Insertion-ordered
    }

    /** Returns the entire ledger (preserves insertion order — oldest first) */
    public Map<String, String> getAuditLedger() {
        return Collections.unmodifiableMap(tradeAuditLedger);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TreeMap — sorted portfolio (ascending alphabetical by symbol)
    // ─────────────────────────────────────────────────────────────────────────

    /** Adds or updates a holding in the sorted portfolio */
    public void updatePortfolioHolding(String symbol, double currentValue) {
        sortedPortfolio.put(symbol, currentValue);  // TreeMap keeps keys sorted
    }

    /** Returns portfolio sorted by symbol (TreeMap natural ordering) */
    public Map<String, Double> getSortedPortfolio() {
        return Collections.unmodifiableMap(sortedPortfolio); // Keys already sorted A→Z
    }

    /** Returns the first (alphabetically lowest) stock symbol in the portfolio */
    public String getFirstHolding() {
        return sortedPortfolio.isEmpty() ? null : sortedPortfolio.firstKey();
    }

    /** Returns the last (alphabetically highest) stock symbol in the portfolio */
    public String getLastHolding() {
        return sortedPortfolio.isEmpty() ? null : sortedPortfolio.lastKey();
    }
}
