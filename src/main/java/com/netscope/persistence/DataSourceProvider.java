package com.netscope.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Loads database connection settings from {@code application.properties}
 * on the classpath and provides new JDBC connections on demand.
 *
 * <p>Credentials are never hardcoded in source — they are read from a
 * properties file that is excluded from version control, so the
 * repository never contains a real password. A committed
 * {@code application.properties.example} documents the expected keys.
 *
 * <p>This class does not pool connections; each call to
 * {@link #getConnection()} opens a new one. Connection pooling is out
 * of scope for this stage — callers are responsible for closing the
 * connection they receive, ideally via try-with-resources.
 */
public final class DataSourceProvider {

    private final String url;
    private final String username;
    private final String password;

    /**
     * Loads settings from {@code application.properties} on the classpath.
     *
     * @throws PersistenceException if the properties file is missing or unreadable
     */
    public DataSourceProvider() {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new PersistenceException(
                        "application.properties not found on classpath. " +
                                "Copy application.properties.example to application.properties and fill in your credentials.");
            }
            properties.load(input);

        } catch (IOException e) {
            throw new PersistenceException("Failed to load application.properties", e);
        }

        this.url = requireProperty(properties, "db.url");
        this.username = requireProperty(properties, "db.username");
        this.password = requireProperty(properties, "db.password");
    }

    private String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new PersistenceException("Missing required property: " + key);
        }
        return value;
    }

    /**
     * Opens a new JDBC connection using the loaded settings.
     *
     * @return a new, open {@link Connection}
     * @throws PersistenceException if the connection cannot be established
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new PersistenceException("Failed to connect to database at " + url, e);
        }
    }
}