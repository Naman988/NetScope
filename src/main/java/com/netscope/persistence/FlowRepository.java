package com.netscope.persistence;

import com.netscope.flow.Flow;
import com.netscope.flow.FlowKey;
import com.netscope.model.Protocol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * Persists and retrieves {@link Flow} records, identified by their
 * {@link FlowKey}, using raw JDBC.
 *
 * <p>No ORM is used — this class issues its own SQL directly via
 * {@link PreparedStatement}, matching the pipeline's stated design
 * choice to avoid Hibernate/JPA. Every JDBC resource is opened in a
 * try-with-resources block so connections, statements, and result
 * sets are always closed, even on exception.
 */
public final class FlowRepository {

    private final DataSourceProvider dataSourceProvider;

    public FlowRepository(DataSourceProvider dataSourceProvider) {
        if (dataSourceProvider == null) {
            throw new IllegalArgumentException("dataSourceProvider must not be null");
        }
        this.dataSourceProvider = dataSourceProvider;
    }

    /**
     * Inserts a new flow row for the given key and its current stats.
     *
     * @param key  the flow's identifying 5-tuple
     * @param flow the flow's current statistics
     * @return the generated database ID for the inserted row
     * @throws PersistenceException if the insert fails
     */
    public long insertFlow(FlowKey key, Flow flow) {
        String sql = "INSERT INTO flows " +
                "(endpoint_a_ip, endpoint_a_port, endpoint_b_ip, endpoint_b_port, protocol, " +
                " packet_count, byte_count, first_seen, last_seen) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSourceProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, key.getEndpointAIp());
            statement.setInt(2, key.getEndpointAPort());
            statement.setString(3, key.getEndpointBIp());
            statement.setInt(4, key.getEndpointBPort());
            statement.setString(5, key.getProtocol().name());
            statement.setLong(6, flow.getPacketCount());
            statement.setLong(7, flow.getByteCount());
            statement.setTimestamp(8, Timestamp.from(flow.getFirstSeen()));
            statement.setTimestamp(9, Timestamp.from(flow.getLastSeen()));

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
                throw new PersistenceException("Insert succeeded but no generated key was returned");
            }

        } catch (SQLException e) {
            throw new PersistenceException("Failed to insert flow for key " + key, e);
        }
    }

    /**
     * Reads back a single flow row by its database ID.
     *
     * @param flowId the generated ID returned by {@link #insertFlow}
     * @return the stored flow's data, or empty if no row exists with that ID
     * @throws PersistenceException if the query fails
     */
    public Optional<StoredFlow> findById(long flowId) {
        String sql = "SELECT id, endpoint_a_ip, endpoint_a_port, endpoint_b_ip, endpoint_b_port, " +
                "protocol, packet_count, byte_count, first_seen, last_seen " +
                "FROM flows WHERE id = ?";

        try (Connection connection = dataSourceProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, flowId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new StoredFlow(
                        resultSet.getLong("id"),
                        resultSet.getString("endpoint_a_ip"),
                        resultSet.getInt("endpoint_a_port"),
                        resultSet.getString("endpoint_b_ip"),
                        resultSet.getInt("endpoint_b_port"),
                        Protocol.valueOf(resultSet.getString("protocol")),
                        resultSet.getLong("packet_count"),
                        resultSet.getLong("byte_count"),
                        resultSet.getTimestamp("first_seen").toInstant(),
                        resultSet.getTimestamp("last_seen").toInstant()
                ));
            }

        } catch (SQLException e) {
            throw new PersistenceException("Failed to read flow with id " + flowId, e);
        }
    }

    /**
     * Read-only projection of a persisted flow row. Kept separate from
     * {@link Flow}/{@link FlowKey} since those are in-memory tracking
     * types, not database-mapped records — this avoids conflating the
     * two concerns.
     */
    public record StoredFlow(
            long id,
            String endpointAIp,
            int endpointAPort,
            String endpointBIp,
            int endpointBPort,
            Protocol protocol,
            long packetCount,
            long byteCount,
            java.time.Instant firstSeen,
            java.time.Instant lastSeen
    ) {}

    /**
     * Reads back every flow currently stored.
     *
     * @return all flows, ordered by ID
     * @throws PersistenceException if the query fails
     */
    public java.util.List<StoredFlow> findAll() {
        String sql = "SELECT id, endpoint_a_ip, endpoint_a_port, endpoint_b_ip, endpoint_b_port, " +
                "protocol, packet_count, byte_count, first_seen, last_seen " +
                "FROM flows ORDER BY id";

        java.util.List<StoredFlow> results = new java.util.ArrayList<>();

        try (Connection connection = dataSourceProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                results.add(new StoredFlow(
                        resultSet.getLong("id"),
                        resultSet.getString("endpoint_a_ip"),
                        resultSet.getInt("endpoint_a_port"),
                        resultSet.getString("endpoint_b_ip"),
                        resultSet.getInt("endpoint_b_port"),
                        Protocol.valueOf(resultSet.getString("protocol")),
                        resultSet.getLong("packet_count"),
                        resultSet.getLong("byte_count"),
                        resultSet.getTimestamp("first_seen").toInstant(),
                        resultSet.getTimestamp("last_seen").toInstant()
                ));
            }
            return results;

        } catch (SQLException e) {
            throw new PersistenceException("Failed to read all flows", e);
        }
    }
}