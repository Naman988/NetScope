package com.netscope.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RawPacketTest {

    private PacketMetadata sampleMetadata() {
        return new PacketMetadata(Instant.now(), 60, 60, "eth0");
    }

    @Test
    void packetId_shouldBeUnique() {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        RawPacket p1 = new RawPacket(data, sampleMetadata());
        RawPacket p2 = new RawPacket(data, sampleMetadata());

        assertNotEquals(p1.getPacketId(), p2.getPacketId());
    }

    @Test
    void getRawData_shouldReturnDefensiveCopy() {
        byte[] original = new byte[]{0x01, 0x02, 0x03};
        RawPacket packet = new RawPacket(original, sampleMetadata());

        byte[] retrieved = packet.getRawData();
        retrieved[0] = (byte) 0xFF; // mutate the copy

        assertEquals(0x01, packet.getRawData()[0]); // original must be unchanged
    }

    @Test
    void constructor_shouldRejectNullData() {
        assertThrows(IllegalArgumentException.class,
                () -> new RawPacket(null, sampleMetadata()));
    }

    @Test
    void constructor_shouldRejectEmptyData() {
        assertThrows(IllegalArgumentException.class,
                () -> new RawPacket(new byte[]{}, sampleMetadata()));
    }

    @Test
    void isTruncated_shouldReturnTrue_whenCapturedLessThanOriginal() {
        PacketMetadata meta = new PacketMetadata(Instant.now(), 100, 60, null);
        assertTrue(meta.isTruncated());
    }

    @Test
    void interfaceName_shouldBeEmpty_whenNull() {
        PacketMetadata meta = new PacketMetadata(Instant.now(), 60, 60, null);
        assertTrue(meta.getInterfaceName().isEmpty());
    }
}