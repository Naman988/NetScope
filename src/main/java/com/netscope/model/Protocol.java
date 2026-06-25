package com.netscope.model;

/**
 * Represents the network protocols that NetScope recognizes at the
 * packet acquisition stage (Layers 2, 3, and 4).
 *
 * <p>Layer 7 protocols (HTTP, TLS, DNS) are intentionally excluded.
 * Those are identified by the parser and classifier modules, not here.
 *
 * <p>UNKNOWN is the defensive default for any unrecognized protocol,
 * ensuring the pipeline never throws on unexpected traffic.
 */
public enum Protocol {

    ETHERNET("Ethernet"),
    ARP("ARP"),
    IPV4("IPv4"),
    IPV6("IPv6"),
    TCP("TCP"),
    UDP("UDP"),
    ICMP("ICMP"),
    UNKNOWN("Unknown");

    private final String displayName;

    Protocol(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns a human-readable name suitable for reports and logs.
     *
     * @return display name of this protocol
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}