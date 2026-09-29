package com.netscope.parser;

/**
 * Checked exception representing a genuine failure to parse a
 * packet's raw bytes — malformed or corrupted data that the
 * underlying capture library's own parser rejects.
 *
 * <p>This is distinct from "protocol not specially handled": an
 * unrecognized-but-well-formed protocol (e.g. ARP) is not an error
 * and results in a {@code ParsedPacket} with
 * {@link com.netscope.model.Protocol#UNKNOWN}, not an exception.
 * This exception exists only for bytes that cannot be interpreted
 * as a valid packet at all.
 */
public class PacketParseException extends Exception {

    /**
     * @param message human-readable description of what went wrong
     */
    public PacketParseException(String message) {
        super(message);
    }

    /**
     * @param message human-readable description of what went wrong
     * @param cause   the underlying exception that triggered this failure
     */
    public PacketParseException(String message, Throwable cause) {
        super(message, cause);
    }
}