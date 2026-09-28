package com.netscope.reader;

import com.netscope.model.RawPacket;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PcapFileReaderTest {

    private String sampleFilePath() throws Exception {
        URL resource = getClass().getClassLoader().getResource("sample.pcap");
        assertNotNull(resource, "sample.pcap must exist in src/test/resources");
        return Paths.get(resource.toURI()).toString();
    }

    @Test
    void readPackets_returnsNonEmptyListFromSampleFile() throws Exception {
        PcapFileReader reader = new PcapFileReader(sampleFilePath());
        List<RawPacket> packets = reader.readPackets();

        assertFalse(packets.isEmpty(), "Expected at least one packet to be read");
    }

    @Test
    void readPackets_firstPacketHasSaneMetadata() throws Exception {
        PcapFileReader reader = new PcapFileReader(sampleFilePath());
        List<RawPacket> packets = reader.readPackets();

        RawPacket first = packets.get(0);
        assertTrue(first.size() > 0);
        assertNotNull(first.getMetadata().getCaptureTimestamp());
        assertTrue(first.getMetadata().getCapturedLength() <= first.getMetadata().getOriginalLength());
    }

    @Test
    void readPackets_throwsForNonexistentFile() {
        PcapFileReader reader = new PcapFileReader("no/such/file.pcap");
        assertThrows(PacketReadException.class, reader::readPackets);
    }
}