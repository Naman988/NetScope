package com.netscope.persistence;

/**
 * Unchecked exception representing a failure to read persistence
 * configuration or communicate with the database.
 *
 * <p>Unchecked rather than checked, unlike {@code PacketReadException}
 * and {@code PacketParseException}: a database failure here is
 * typically a configuration or connectivity problem that callers
 * throughout the call stack should not be forced to declare or catch
 * individually — it is closer to a startup/environment failure than
 * an expected, recoverable condition in the packet pipeline.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}