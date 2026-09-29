package com.netscope.parser;

import com.netscope.model.ParsedPacket;
import com.netscope.model.Protocol;
import com.netscope.model.RawPacket;
import com.netscope.reader.PcapFileReader;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPacketParserTest {

    private List<RawPacket> loadSamplePackets() throws Exception {
        URL resource = getClass().getClassLoader().getResource("sample.pcap");
        assertNotNull(resource, "sample.pcap must exist in src/test/resources");
        String path = Paths.get(resource.toURI()).toString();
        return new PcapFileReader(path).readPackets();
    }

    @Test
    void parse_extractsIpAndPortForTcpPacket() throws Exception {
        List<RawPacket> rawPackets = loadSamplePackets();
        DefaultPacketParser parser = new DefaultPacketParser();

        boolean foundTcp = false;
        for (RawPacket raw : rawPackets) {
            ParsedPacket parsed = parser.parse(raw);
            if (parsed.getProtocol() == Protocol.TCP) {
                foundTcp = true;
                assertNotNull(parsed.getSourceIp());
                assertNotNull(parsed.getDestinationIp());
                assertNotNull(parsed.getSourcePort());
                assertNotNull(parsed.getDestinationPort());
                assertSame(raw, parsed.getRawPacket());
            }
        }

        assertTrue(foundTcp, "Expected at least one TCP packet in sample.pcap (it's an HTTP capture)");
    }

    @Test
    void parse_neverThrowsForWellFormedNonIpPackets() throws Exception {
        // Every packet in a valid capture file should parse without
        // exception, even if some resolve to Protocol.UNKNOWN.
        List<RawPacket> rawPackets = loadSamplePackets();
        DefaultPacketParser parser = new DefaultPacketParser();

        for (RawPacket raw : rawPackets) {
            assertDoesNotThrow(() -> parser.parse(raw));
        }
    }
}