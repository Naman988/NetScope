package com.netscope.flow;

import com.netscope.model.ParsedPacket;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains the set of active flows observed so far, keyed by
 * {@link FlowKey}, and updates each flow's statistics as packets
 * arrive.
 *
 * <p>Backed by {@link ConcurrentHashMap} so that flow lookups and
 * insertions are safe under concurrent access — relevant once a
 * later sprint introduces multithreaded packet processing, though
 * nothing about this sprint requires that yet.
 *
 * <p>Only packets with both a source and destination port are
 * tracked as flows (i.e. TCP/UDP). A {@link ParsedPacket} with
 * null ports (ICMP, ARP, unrecognized protocols) has no meaningful
 * 5-tuple and is skipped rather than forced into a degenerate key.
 */
public final class FlowTracker {

    private final ConcurrentHashMap<FlowKey, Flow> flows = new ConcurrentHashMap<>();

    /**
     * Updates flow statistics for the given packet, creating a new
     * {@link Flow} if this is the first packet seen for its
     * {@link FlowKey}.
     *
     * @param parsedPacket the packet to record; must not be null
     * @return true if the packet was recorded, false if it was
     *         skipped because it had no port information
     */
    public boolean recordPacket(ParsedPacket parsedPacket) {
        if (parsedPacket == null) {
            throw new IllegalArgumentException("parsedPacket must not be null");
        }

        Integer sourcePort = parsedPacket.getSourcePort();
        Integer destinationPort = parsedPacket.getDestinationPort();

        if (sourcePort == null || destinationPort == null
                || parsedPacket.getSourceIp() == null || parsedPacket.getDestinationIp() == null) {
            return false;
        }

        FlowKey key = new FlowKey(
                parsedPacket.getSourceIp(), sourcePort,
                parsedPacket.getDestinationIp(), destinationPort,
                parsedPacket.getProtocol());

        Flow flow = flows.computeIfAbsent(key, k -> new Flow());
        flow.recordPacket(
                parsedPacket.getRawPacket().size(),
                parsedPacket.getRawPacket().getMetadata().getCaptureTimestamp());

        return true;
    }

    /** @return the flow tracked for the given key, or null if none exists */
    public Flow getFlow(FlowKey key) {
        return flows.get(key);
    }

    /** @return an unmodifiable-in-spirit view of all currently tracked flows */
    public Collection<Flow> getAllFlows() {
        return flows.values();
    }

    /** @return the number of distinct flows currently tracked */
    public int flowCount() {
        return flows.size();
    }
}