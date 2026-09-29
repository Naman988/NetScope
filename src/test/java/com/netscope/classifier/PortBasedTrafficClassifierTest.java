package com.netscope.classifier;

import com.netscope.model.ApplicationProtocol;
import com.netscope.model.PacketMetadata;
import com.netscope.model.ParsedPacket;
import com.netscope.model.Protocol;
import com.netscope.model.RawPacket;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PortBasedTrafficClassifierTest {

    private final PortBasedTrafficClassifier classifier = new PortBasedTrafficClassifier();

    private ParsedPacket parsedPacketWithPorts(Integer sourcePort, Integer destinationPort) {
        RawPacket rawPacket = new RawPacket(
                new byte[]{1, 2, 3},
                new PacketMetadata(Instant.now(), 3, 3, null));

        return new ParsedPacket(
                rawPacket, Protocol.TCP, "10.0.0.1", "10.0.0.2", sourcePort, destinationPort);
    }

    @Test
    void classify_recognizesHttpByDestinationPort() {
        ParsedPacket packet = parsedPacketWithPorts(51000, 80);
        assertEquals(ApplicationProtocol.HTTP, classifier.classify(packet));
    }

    @Test
    void classify_recognizesHttpsByDestinationPort() {
        ParsedPacket packet = parsedPacketWithPorts(51000, 443);
        assertEquals(ApplicationProtocol.HTTPS, classifier.classify(packet));
    }

    @Test
    void classify_recognizesDnsBySourcePort_forResponsePackets() {
        // A DNS response has port 53 as the SOURCE port, not destination.
        ParsedPacket packet = parsedPacketWithPorts(53, 51000);
        assertEquals(ApplicationProtocol.DNS, classifier.classify(packet));
    }

    @Test
    void classify_fallsBackToUnknownForUnrecognizedPort() {
        ParsedPacket packet = parsedPacketWithPorts(51000, 8080);
        assertEquals(ApplicationProtocol.UNKNOWN, classifier.classify(packet));
    }

    @Test
    void classify_fallsBackToUnknownWhenPortsAreNull() {
        ParsedPacket packet = parsedPacketWithPorts(null, null);
        assertEquals(ApplicationProtocol.UNKNOWN, classifier.classify(packet));
    }
}