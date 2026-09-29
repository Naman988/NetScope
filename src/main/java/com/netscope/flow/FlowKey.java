package com.netscope.flow;

import com.netscope.model.Protocol;

import java.util.Objects;

/**
 * Immutable value object identifying a network flow by its 5-tuple:
 * source IP, source port, destination IP, destination port, and
 * protocol.
 *
 * <p><b>Direction is normalized.</b> A conversation between two
 * endpoints produces the same {@code FlowKey} regardless of which
 * direction a given packet travels — a request (A -> B) and its
 * response (B -> A) are treated as the same flow, matching the
 * convention used by tools like NetFlow and Wireshark's
 * "Conversations" view. This is done by canonically ordering the
 * two endpoints (by IP, then by port) at construction time, rather
 * than trusting whichever order the caller passed source/destination
 * in.
 *
 * <p>Because of this normalization, {@link #equals(Object)} and
 * {@link #hashCode()} are based on the two endpoints as an
 * unordered pair plus protocol — this is what makes
 * {@code ConcurrentHashMap<FlowKey, Flow>} lookups in
 * {@link FlowTracker} correctly group both directions of a
 * conversation under one key.
 */
public final class FlowKey {

    private final String endpointAIp;
    private final int endpointAPort;
    private final String endpointBIp;
    private final int endpointBPort;
    private final Protocol protocol;

    /**
     * Constructs a {@code FlowKey} from a packet's observed
     * source/destination pair, normalizing direction internally.
     *
     * @param sourceIp        observed source IP; must not be null
     * @param sourcePort      observed source port
     * @param destinationIp   observed destination IP; must not be null
     * @param destinationPort observed destination port
     * @param protocol        transport protocol; must not be null
     */
    public FlowKey(String sourceIp, int sourcePort,
                   String destinationIp, int destinationPort,
                   Protocol protocol) {

        if (sourceIp == null) {
            throw new IllegalArgumentException("sourceIp must not be null");
        }
        if (destinationIp == null) {
            throw new IllegalArgumentException("destinationIp must not be null");
        }
        if (protocol == null) {
            throw new IllegalArgumentException("protocol must not be null");
        }

        // Canonical ordering: compare IP first, then port, so the same
        // conversation always produces identical endpointA/endpointB
        // assignment regardless of which direction this packet travelled.
        int ipComparison = sourceIp.compareTo(destinationIp);
        boolean sourceIsCanonicalFirst =
                ipComparison < 0 || (ipComparison == 0 && sourcePort <= destinationPort);

        if (sourceIsCanonicalFirst) {
            this.endpointAIp = sourceIp;
            this.endpointAPort = sourcePort;
            this.endpointBIp = destinationIp;
            this.endpointBPort = destinationPort;
        } else {
            this.endpointAIp = destinationIp;
            this.endpointAPort = destinationPort;
            this.endpointBIp = sourceIp;
            this.endpointBPort = sourcePort;
        }

        this.protocol = protocol;
    }

    /** @return the transport protocol for this flow */
    public Protocol getProtocol() {
        return protocol;
    }

    /** @return the canonically-first endpoint's IP */
    public String getEndpointAIp() {
        return endpointAIp;
    }

    /** @return the canonically-first endpoint's port */
    public int getEndpointAPort() {
        return endpointAPort;
    }

    /** @return the canonically-second endpoint's IP */
    public String getEndpointBIp() {
        return endpointBIp;
    }

    /** @return the canonically-second endpoint's port */
    public int getEndpointBPort() {
        return endpointBPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlowKey other)) return false;
        return endpointAPort == other.endpointAPort
                && endpointBPort == other.endpointBPort
                && protocol == other.protocol
                && endpointAIp.equals(other.endpointAIp)
                && endpointBIp.equals(other.endpointBIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointAIp, endpointAPort, endpointBIp, endpointBPort, protocol);
    }

    @Override
    public String toString() {
        return "FlowKey{" +
                endpointAIp + ":" + endpointAPort +
                " <-> " + endpointBIp + ":" + endpointBPort +
                ", protocol=" + protocol +
                '}';
    }
}