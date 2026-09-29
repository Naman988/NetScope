package com.netscope.classifier;

import com.netscope.model.ApplicationProtocol;
import com.netscope.model.ParsedPacket;

import java.util.Map;

/**
 * {@link TrafficClassifier} implementation that infers the
 * application protocol from well-known port numbers.
 *
 * <p>This is intentionally the simplest possible classification
 * strategy: an honest port-to-protocol lookup, nothing more. It
 * makes no claim to inspect payloads or handle protocols running
 * on non-standard ports — that's a limitation worth stating plainly
 * rather than disguising, and it's exactly why {@link TrafficClassifier}
 * is an interface: a smarter implementation can replace this one
 * later without downstream packages changing.
 *
 * <p>Checks both the destination and source port against the known
 * map, since a response packet's well-known port is often in the
 * source position rather than the destination.
 *
 * <p>Stateless and thread-safe.
 */
public final class PortBasedTrafficClassifier implements TrafficClassifier {

    private static final Map<Integer, ApplicationProtocol> PORT_MAP = Map.of(
            80, ApplicationProtocol.HTTP,
            443, ApplicationProtocol.HTTPS,
            53, ApplicationProtocol.DNS
    );

    @Override
    public ApplicationProtocol classify(ParsedPacket parsedPacket) {
        if (parsedPacket == null) {
            throw new IllegalArgumentException("parsedPacket must not be null");
        }

        Integer destinationPort = parsedPacket.getDestinationPort();
        Integer sourcePort = parsedPacket.getSourcePort();

        if (destinationPort != null && PORT_MAP.containsKey(destinationPort)) {
            return PORT_MAP.get(destinationPort);
        }

        if (sourcePort != null && PORT_MAP.containsKey(sourcePort)) {
            return PORT_MAP.get(sourcePort);
        }

        return ApplicationProtocol.UNKNOWN;
    }
}