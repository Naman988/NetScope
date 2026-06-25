package com.netscope.model;

import java.time.Instant;
import java.util.Optional;

/**
 * Immutable value object representing the contextual information
 * about a captured packet — separate from its raw payload bytes.
 *
 * <p>Analogous to the {@code pcap_pkthdr} structure in libpcap:
 * it carries the envelope (when, how big, from where) without
 * carrying the letter (the actual packet bytes).
 *
 * <p>All fields are final. This class is safe to share across
 * threads without synchronization.
 */
public final class PacketMetadata {

    /**
     * The exact moment this packet was captured.
     * Stored as {@link Instant} for timezone independence.
     * Use {@code captureTimestamp.toEpochMilli()} for JDBC persistence.
     */
    private final Instant captureTimestamp;

    /**
     * The actual size of the packet on the wire, in bytes.
     * May be larger than {@code capturedLength} if the capture was truncated.
     */
    private final int originalLength;

    /**
     * The number of bytes actually stored in this capture.
     * PCAP files can truncate packets using the snaplen setting.
     * Always {@code <= originalLength}.
     */
    private final int capturedLength;

    /**
     * The network interface that captured this packet.
     * Nullable because PCAP files do not always carry interface metadata.
     */
    private final String interfaceName;

    /**
     * Constructs a {@code PacketMetadata} instance.
     *
     * @param captureTimestamp when the packet was captured; must not be null
     * @param originalLength   actual size of the packet on the wire
     * @param capturedLength   number of bytes stored in the capture
     * @param interfaceName    name of the capture interface; may be null
     * @throws IllegalArgumentException if capturedLength exceeds originalLength
     */
    public PacketMetadata(Instant captureTimestamp,
                          int originalLength,
                          int capturedLength,
                          String interfaceName) {

        if (captureTimestamp == null) {
            throw new IllegalArgumentException("captureTimestamp must not be null");
        }
        if (capturedLength > originalLength) {
            throw new IllegalArgumentException(
                    "capturedLength (" + capturedLength + ") cannot exceed " +
                            "originalLength (" + originalLength + ")"
            );
        }

        this.captureTimestamp = captureTimestamp;
        this.originalLength   = originalLength;
        this.capturedLength   = capturedLength;
        this.interfaceName    = interfaceName;
    }

    /** @return the timestamp when this packet was captured */
    public Instant getCaptureTimestamp() {
        return captureTimestamp;
    }

    /** @return the original size of the packet on the wire, in bytes */
    public int getOriginalLength() {
        return originalLength;
    }

    /** @return the number of bytes actually captured */
    public int getCapturedLength() {
        return capturedLength;
    }

    /**
     * Returns the network interface name, if available.
     * PCAP files do not always include this information.
     *
     * @return an {@link Optional} containing the interface name, or empty
     */
    public Optional<String> getInterfaceName() {
        return Optional.ofNullable(interfaceName);
    }

    /**
     * Returns true if the packet was truncated during capture,
     * meaning {@code capturedLength < originalLength}.
     *
     * @return true if this packet is truncated
     */
    public boolean isTruncated() {
        return capturedLength < originalLength;
    }

    @Override
    public String toString() {
        return "PacketMetadata{" +
                "timestamp=" + captureTimestamp +
                ", originalLength=" + originalLength +
                ", capturedLength=" + capturedLength +
                ", interface=" + (interfaceName != null ? interfaceName : "N/A") +
                ", truncated=" + isTruncated() +
                '}';
    }
}