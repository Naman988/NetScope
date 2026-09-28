package com.netscope.reader;

/**
 * Checked exception representing a failure to read or translate
 * packets from a capture source.
 *
 * <p>This exception exists so that no code outside the {@code reader}
 * package ever needs to know about, or catch, third-party library
 * exception types. Any failure originating from the underlying
 * capture library is wrapped in a {@code PacketReadException} before
 * it crosses the package boundary — this is part of what makes the
 * reader package an Anti-Corruption Layer.
 */
public class PacketReadException extends Exception {

    /**
     * Constructs a new exception with a descriptive message only.
     *
     * @param message human-readable description of what went wrong
     */
    public PacketReadException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception wrapping an underlying cause.
     *
     * <p>Use this overload when translating an exception thrown by
     * the underlying capture library, so the original stack trace
     * and cause chain are preserved for debugging.
     *
     * @param message human-readable description of what went wrong
     * @param cause   the underlying exception that triggered this failure
     */
    public PacketReadException(String message, Throwable cause) {
        super(message, cause);
    }
}