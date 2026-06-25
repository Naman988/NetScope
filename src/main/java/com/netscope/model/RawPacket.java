package com.netscope.model;

import java.util.Arrays;
import java.util.UUID;

/**
 * Immutable domain model representing a single packet as captured
 * from a PCAP file — before any parsing or analysis.
 *
 * <p>This is the primary data structure that flows through the
 * NetScope pipeline. Every module downstream of the reader
 * operates on {@code RawPacket} instances.
 *
 * <p>{@code RawPacket} knows only what was observed at capture time:
 * the raw bytes and their metadata. It has no knowledge of protocols,
 * applications, or flows — that is the responsibility of later stages.
 *
 * <p>Immutability guarantee: {@code rawData} is defensively copied on
 * construction and on access, so no external code can mutate the
 * packet's bytes after creation.
 *
 * <p>Thread safety: this class is unconditionally thread-safe.
 */
public final class RawPacket {

    /**
     * Globally unique identifier for this packet within a session.
     * Using UUID ensures uniqueness across threads with no shared counter.
     * Persisted as VARCHAR(36) in the database.
     */
    private final UUID packetId;

    /**
     * The raw bytes of the captured packet.
     * Stored as a defensive copy to preserve immutability.
     */
    private final byte[] rawData;

    /**
     * Contextual metadata about this packet (timestamp, lengths, interface).
     * Kept separate from payload following the Single Responsibility Principle.
     */
    private final PacketMetadata metadata;

    /**
     * Constructs a {@code RawPacket} with an auto-generated UUID.
     *
     * @param rawData  the raw captured bytes; must not be null or empty
     * @param metadata the packet's capture metadata; must not be null
     * @throws IllegalArgumentException if rawData is null or empty
     */
    public RawPacket(byte[] rawData, PacketMetadata metadata) {
        if (rawData == null || rawData.length == 0) {
            throw new IllegalArgumentException("rawData must not be null or empty");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("metadata must not be null");
        }

        this.packetId = UUID.randomUUID();
        this.rawData  = Arrays.copyOf(rawData, rawData.length); // defensive copy
        this.metadata = metadata;
    }

    /**
     * Returns the unique identifier for this packet.
     *
     * @return UUID assigned at construction time
     */
    public UUID getPacketId() {
        return packetId;
    }

    /**
     * Returns a defensive copy of the raw packet bytes.
     *
     * <p>A copy is returned each time to prevent callers from
     * mutating the internal state of this packet.
     *
     * @return copy of the raw captured bytes
     */
    public byte[] getRawData() {
        return Arrays.copyOf(rawData, rawData.length);
    }

    /**
     * Returns the capture metadata associated with this packet.
     *
     * @return this packet's {@link PacketMetadata}
     */
    public PacketMetadata getMetadata() {
        return metadata;
    }

    /**
     * Returns the number of bytes in the captured payload.
     * Convenience method equivalent to {@code getRawData().length}.
     *
     * @return captured byte count
     */
    public int size() {
        return rawData.length;
    }

    /**
     * Returns a concise human-readable summary of this packet,
     * suitable for logging and debugging.
     *
     * @return summary string
     */
    public String getSummary() {
        return "RawPacket{" +
                "id=" + packetId +
                ", size=" + rawData.length + " bytes" +
                ", " + metadata +
                '}';
    }

    @Override
    public String toString() {
        return getSummary();
    }
}