package com.netscope.reader;

import com.netscope.model.PacketMetadata;
import com.netscope.model.RawPacket;
import org.pcap4j.packet.Packet;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Translates packets from the underlying capture library's own
 * representation into NetScope's immutable domain model.
 *
 * <p>This class is the Anti-Corruption Layer itself: it is the
 * single point in the codebase where a third-party packet type
 * is converted into {@link RawPacket}. No class outside the
 * {@code reader} package should ever need to import a capture
 * library type directly.
 *
 * <p>Stateless and thread-safe: holds no fields.
 */
public final class PacketAdapter {

    private PacketAdapter() {
        // Utility class — not meant to be instantiated.
    }

    /**
     * Converts a captured packet and its capture-time details into a
     * {@link RawPacket}.
     *
     * @param packet         the packet as returned by the capture library
     * @param timestamp      capture time reported by the capture handle;
     *                       may be null, in which case the current time is used
     * @param originalLength size of the packet on the wire, in bytes
     * @param interfaceName  name of the capturing interface, or null if unavailable
     * @return an immutable {@link RawPacket}
     */
    public static RawPacket toRawPacket(Packet packet,
                                        Timestamp timestamp,
                                        int originalLength,
                                        String interfaceName) {
        byte[] rawBytes = packet.getRawData();

        Instant captureTimestamp = (timestamp != null)
                ? timestamp.toInstant()
                : Instant.now();

        int capturedLength = rawBytes.length;

        // PacketMetadata requires capturedLength <= originalLength.
        // Some sources report originalLength as 0 or unset, so guard it.
        if (originalLength < capturedLength) {
            originalLength = capturedLength;
        }

        PacketMetadata metadata = new PacketMetadata(
                captureTimestamp, originalLength, capturedLength, interfaceName);

        return new RawPacket(rawBytes, metadata);
    }
}