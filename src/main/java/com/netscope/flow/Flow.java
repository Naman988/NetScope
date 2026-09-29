package com.netscope.flow;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks running statistics for a single network flow: packet
 * count, byte count, and first/last-seen timestamps.
 *
 * <p>Unlike the pipeline's domain models so far, {@code Flow} is
 * intentionally mutable — its whole purpose is to accumulate state
 * as packets belonging to its {@link FlowKey} arrive. Thread safety
 * is achieved with atomics and synchronized timestamp updates
 * rather than by making the object immutable, since
 * {@link FlowTracker} may update the same {@code Flow} instance
 * from multiple threads.
 */
public final class Flow {

    private final AtomicLong packetCount = new AtomicLong(0);
    private final AtomicLong byteCount = new AtomicLong(0);

    private volatile Instant firstSeen;
    private volatile Instant lastSeen;

    /**
     * Records one observed packet belonging to this flow.
     *
     * @param packetSizeBytes size of the packet in bytes; must be >= 0
     * @param observedAt      when this packet was captured
     */
    public synchronized void recordPacket(int packetSizeBytes, Instant observedAt) {
        if (packetSizeBytes < 0) {
            throw new IllegalArgumentException("packetSizeBytes must not be negative");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("observedAt must not be null");
        }

        packetCount.incrementAndGet();
        byteCount.addAndGet(packetSizeBytes);

        if (firstSeen == null || observedAt.isBefore(firstSeen)) {
            firstSeen = observedAt;
        }
        if (lastSeen == null || observedAt.isAfter(lastSeen)) {
            lastSeen = observedAt;
        }
    }

    /** @return total number of packets recorded for this flow so far */
    public long getPacketCount() {
        return packetCount.get();
    }

    /** @return total number of bytes recorded for this flow so far */
    public long getByteCount() {
        return byteCount.get();
    }

    /** @return timestamp of the earliest packet recorded, or null if none recorded yet */
    public Instant getFirstSeen() {
        return firstSeen;
    }

    /** @return timestamp of the most recent packet recorded, or null if none recorded yet */
    public Instant getLastSeen() {
        return lastSeen;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "packets=" + packetCount.get() +
                ", bytes=" + byteCount.get() +
                ", firstSeen=" + firstSeen +
                ", lastSeen=" + lastSeen +
                '}';
    }
}