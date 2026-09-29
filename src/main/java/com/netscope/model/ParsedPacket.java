package com.netscope.model;

/**
 * Immutable domain model representing a packet after protocol and
 * address extraction has been performed on its raw bytes.
 *
 * <p>{@code ParsedPacket} enriches a {@link RawPacket} with the
 * structured information the parser package extracts from it —
 * protocol, source/destination IP, and source/destination port —
 * without discarding the original raw data. Downstream packages
 * (classifier, flow tracker, rule engine) operate on this class
 * rather than reaching back into raw bytes themselves.
 *
 * <p>Source and destination ports are nullable: not every protocol
 * carries port information (e.g. ICMP, ARP). A null port means
 * "not applicable to this protocol," not "unknown due to a parsing
 * failure" — parsing failures are reported via
 * {@code PacketParseException} instead, before a {@code ParsedPacket}
 * is ever constructed.
 *
 * <p>Thread safety: unconditionally thread-safe, same guarantee as
 * {@link RawPacket}.
 */
public final class ParsedPacket {

    /**
     * The original captured packet this instance was derived from.
     * Retained rather than discarded so later stages (e.g. persistence,
     * debugging, auditing) can still access raw bytes and capture
     * metadata without re-reading the source file.
     */
    private final RawPacket rawPacket;

    /** The protocol identified for this packet. Never null — falls back to {@link Protocol#UNKNOWN}. */
    private final Protocol protocol;

    /** Source IP address as a string (e.g. "192.168.1.10"). Null if not applicable (e.g. non-IP traffic). */
    private final String sourceIp;

    /** Destination IP address as a string. Null if not applicable. */
    private final String destinationIp;

    /** Source port. Null for protocols without ports (ICMP, ARP, etc.). */
    private final Integer sourcePort;

    /** Destination port. Null for protocols without ports. */
    private final Integer destinationPort;

    /**
     * Constructs a {@code ParsedPacket}.
     *
     * @param rawPacket       the original packet this was parsed from; must not be null
     * @param protocol        identified protocol; must not be null (use {@link Protocol#UNKNOWN} if unrecognized)
     * @param sourceIp        source IP address, or null if not applicable
     * @param destinationIp   destination IP address, or null if not applicable
     * @param sourcePort      source port, or null if not applicable
     * @param destinationPort destination port, or null if not applicable
     * @throws IllegalArgumentException if rawPacket or protocol is null
     */
    public ParsedPacket(RawPacket rawPacket,
                        Protocol protocol,
                        String sourceIp,
                        String destinationIp,
                        Integer sourcePort,
                        Integer destinationPort) {

        if (rawPacket == null) {
            throw new IllegalArgumentException("rawPacket must not be null");
        }
        if (protocol == null) {
            throw new IllegalArgumentException("protocol must not be null");
        }

        this.rawPacket = rawPacket;
        this.protocol = protocol;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
    }

    /** @return the original {@link RawPacket} this was derived from */
    public RawPacket getRawPacket() {
        return rawPacket;
    }

    /** @return the identified protocol; never null */
    public Protocol getProtocol() {
        return protocol;
    }

    /** @return source IP address, or null if not applicable to this protocol */
    public String getSourceIp() {
        return sourceIp;
    }

    /** @return destination IP address, or null if not applicable to this protocol */
    public String getDestinationIp() {
        return destinationIp;
    }

    /** @return source port, or null if not applicable to this protocol */
    public Integer getSourcePort() {
        return sourcePort;
    }

    /** @return destination port, or null if not applicable to this protocol */
    public Integer getDestinationPort() {
        return destinationPort;
    }

    @Override
    public String toString() {
        return "ParsedPacket{" +
                "protocol=" + protocol +
                ", " + sourceIp + ":" + sourcePort +
                " -> " + destinationIp + ":" + destinationPort +
                ", rawSize=" + rawPacket.size() +
                '}';
    }
}