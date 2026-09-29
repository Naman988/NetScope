package com.netscope.parser;

import com.netscope.model.ParsedPacket;
import com.netscope.model.RawPacket;

/**
 * Abstraction over "something that extracts protocol and address
 * information from a captured packet."
 *
 * <p>As with {@link com.netscope.reader.PacketReader}, this
 * interface exists so downstream packages (classifier, flow
 * tracker) depend only on this contract, not on a specific parsing
 * strategy or library.
 */
public interface PacketParser {

    /**
     * Extracts protocol and address information from a raw packet.
     *
     * <p>Implementations must never throw for an unrecognized but
     * well-formed protocol — that case is represented by
     * {@link com.netscope.model.Protocol#UNKNOWN} with null address
     * fields in the returned {@link ParsedPacket}. This method
     * throws only for bytes that cannot be parsed as a valid packet
     * at all.
     *
     * @param rawPacket the packet to parse; must not be null
     * @return the parsed result, never null
     * @throws PacketParseException if the packet's bytes are malformed
     */
    ParsedPacket parse(RawPacket rawPacket) throws PacketParseException;
}