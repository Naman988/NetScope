package com.netscope.reader;

import com.netscope.model.RawPacket;

import java.util.List;

/**
 * Abstraction over "a source of captured packets."
 *
 * <p>This interface exists so that every package downstream of
 * {@code reader} depends only on this contract — never on a
 * specific capture library or file format. Today the only
 * implementation reads from a PCAP file, but the rest of the
 * codebase has no knowledge of that; it only knows it can call
 * {@link #readPackets()} and get back {@link RawPacket} instances.
 *
 * <p>This decoupling is what allows the underlying capture
 * mechanism to change later without requiring changes anywhere
 * else in the pipeline.
 */
public interface PacketReader {

    /**
     * Reads all available packets from this reader's source and
     * returns them as immutable domain objects.
     *
     * <p>Implementations are responsible for translating whatever
     * library-specific packet representation they use internally
     * into {@link RawPacket} instances before returning — no
     * library-specific types may escape this method.
     *
     * @return the packets read from the source, in capture order
     * @throws PacketReadException if the source cannot be read or
     *                              a packet cannot be translated
     */
    List<RawPacket> readPackets() throws PacketReadException;
}