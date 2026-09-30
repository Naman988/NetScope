package com.netscope.persistence;

import com.netscope.rule.RuleVerdict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists and retrieves {@link RuleVerdict} evaluations, each tied
 * to a flow via its database ID.
 */
public final class RuleEvaluationRepository {

    private final DataSourceProvider dataSourceProvider;

    public RuleEvaluationRepository(DataSourceProvider dataSourceProvider) {
        if (dataSourceProvider == null) {
            throw new IllegalArgumentException("dataSourceProvider must not be null");
        }
        this.dataSourceProvider = dataSourceProvider;
    }

    /**
     * Inserts a rule evaluation row for the given flow.
     *
     * @param flowId  the database ID of the flow this verdict applies to
     * @param verdict the verdict to persist
     * @return the generated database ID for the inserted row
     * @throws PersistenceException if the insert fails
     */
    public long insertEvaluation(long flowId, RuleVerdict verdict) {
        String sql = "INSERT INTO rule_evaluations (flow_id, verdict, reason) VALUES (?, ?, ?)";

        try (Connection connection = dataSourceProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, flowId);
            statement.setString(2, verdict.isAllowed() ? "ALLOW" : "BLOCK");
            statement.setString(3, verdict.getReason());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
                throw new PersistenceException("Insert succeeded but no generated key was returned");
            }

        } catch (SQLException e) {
            throw new PersistenceException("Failed to insert rule evaluation for flow " + flowId, e);
        }
    }

    /**
     * Reads back all rule evaluations recorded for a given flow.
     *
     * @param flowId the flow's database ID
     * @return the evaluations for that flow, in insertion order
     * @throws PersistenceException if the query fails
     */
    public List<StoredEvaluation> findByFlowId(long flowId) {
        String sql = "SELECT id, flow_id, verdict, reason, evaluated_at " +
                "FROM rule_evaluations WHERE flow_id = ? ORDER BY id";

        List<StoredEvaluation> results = new ArrayList<>();

        try (Connection connection = dataSourceProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, flowId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(new StoredEvaluation(
                            resultSet.getLong("id"),
                            resultSet.getLong("flow_id"),
                            resultSet.getString("verdict"),
                            resultSet.getString("reason"),
                            resultSet.getTimestamp("evaluated_at").toInstant()
                    ));
                }
            }

            return results;

        } catch (SQLException e) {
            throw new PersistenceException("Failed to read rule evaluations for flow " + flowId, e);
        }
    }

    /** Read-only projection of a persisted rule evaluation row. */
    public record StoredEvaluation(
            long id,
            long flowId,
            String verdict,
            String reason,
            java.time.Instant evaluatedAt
    ) {}
}