package com.netscope.flow;

import com.netscope.model.PacketMetadata;
import com.netscope.model.ParsedPacket;
import com.netscope.model.Protocol;
import com.netscope.model.RawPacket;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FlowTrackerTest {

    private ParsedPacket packet(String srcIp, int srcPort, String dstIp, int dstPort, int sizeBytes) {
        byte[] data = new byte[sizeBytes];
        RawPacket rawPacket = new RawPacket(data, new PacketMetadata(Instant.now(), sizeBytes, sizeBytes, null));
        return new ParsedPacket(rawPacket, Protocol.TCP, srcIp, dstIp, srcPort, dstPort);
    }

    @Test
    void recordPacket_createsNewFlowOnFirstPacket() {
        FlowTracker tracker = new FlowTracker();
        boolean recorded = tracker.recordPacket(packet("10.0.0.1", 51000, "10.0.0.2", 80, 100));

        assertTrue(recorded);
        assertEquals(1, tracker.flowCount());
    }

    @Test
    void recordPacket_groupsBothDirectionsIntoOneFlow() {
        FlowTracker tracker = new FlowTracker();

        tracker.recordPacket(packet("10.0.0.1", 51000, "10.0.0.2", 80, 100));  // request
        tracker.recordPacket(packet("10.0.0.2", 80, "10.0.0.1", 51000, 200));  // response

        assertEquals(1, tracker.flowCount());

        FlowKey key = new FlowKey("10.0.0.1", 51000, "10.0.0.2", 80, Protocol.TCP);
        Flow flow = tracker.getFlow(key);

        assertNotNull(flow);
        assertEquals(2, flow.getPacketCount());
        assertEquals(300, flow.getByteCount());
    }

    @Test
    void recordPacket_skipsPacketsWithoutPorts() {
        FlowTracker tracker = new FlowTracker();
        RawPacket rawPacket = new RawPacket(
                new byte[]{1}, new PacketMetadata(Instant.now(), 1, 1, null));
        ParsedPacket icmpLike = new ParsedPacket(
                rawPacket, Protocol.ICMP, "10.0.0.1", "10.0.0.2", null, null);

        boolean recorded = tracker.recordPacket(icmpLike);

        assertFalse(recorded);
        assertEquals(0, tracker.flowCount());
    }
}