package com.netscope.reader;

import com.netscope.model.RawPacket;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * {@link PacketReader} implementation that reads packets from an
 * offline PCAP file on disk.
 *
 * <p>This is the only class permitted to use the capture library's
 * handle and packet types directly. Every packet is translated via
 * {@link PacketAdapter} before being returned.
 *
 * <p>Not thread-safe: one instance reads one file sequentially.
 */
public final class PcapFileReader implements PacketReader {

    private static final Logger logger = LoggerFactory.getLogger(PcapFileReader.class);

    private final String filePath;

    /**
     * @param filePath path to the {@code .pcap} file; must not be null or blank
     */
    public PcapFileReader(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be null or blank");
        }
        this.filePath = filePath;
    }

    @Override
    public List<RawPacket> readPackets() throws PacketReadException {
        List<RawPacket> packets = new ArrayList<>();
        PcapHandle handle = null;

        try {
            handle = Pcaps.openOffline(filePath);

            while (true) {
                Packet packet = handle.getNextPacketEx();
                packets.add(PacketAdapter.toRawPacket(
                        packet,
                        handle.getTimestamp(),
                        handle.getOriginalLength(),
                        null)); // offline files carry no live interface name
            }

        } catch (EOFException e) {
            // Expected: normal end-of-file for offline captures.
            logger.debug("Reached end of PCAP file: {}", filePath);
        } catch (PcapNativeException | NotOpenException e) {
            throw new PacketReadException(
                    "Failed to read packets from file: " + filePath, e);
        } catch (TimeoutException e) {
            throw new PacketReadException(
                    "Timed out reading packet from file: " + filePath, e);
        } finally {
            if (handle != null && handle.isOpen()) {
                handle.close();
            }
        }

        logger.info("Read {} packets from {}", packets.size(), filePath);
        return packets;
    }
}