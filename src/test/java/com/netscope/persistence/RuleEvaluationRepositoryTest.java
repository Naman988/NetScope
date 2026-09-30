package com.netscope.persistence;

import com.netscope.flow.Flow;
import com.netscope.flow.FlowKey;
import com.netscope.model.Protocol;
import com.netscope.rule.RuleVerdict;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleEvaluationRepositoryTest {

    private final DataSourceProvider dataSourceProvider = new DataSourceProvider();
    private final FlowRepository flowRepository = new FlowRepository(dataSourceProvider);
    private final RuleEvaluationRepository evaluationRepository = new RuleEvaluationRepository(dataSourceProvider);

    private Long insertedFlowId;

    @AfterEach
    void cleanUp() throws Exception {
        if (insertedFlowId != null) {
            try (Connection connection = dataSourceProvider.getConnection()) {
                try (PreparedStatement deleteEvaluations = connection.prepareStatement(
                        "DELETE FROM rule_evaluations WHERE flow_id = ?")) {
                    deleteEvaluations.setLong(1, insertedFlowId);
                    deleteEvaluations.executeUpdate();
                }
                try (PreparedStatement deleteFlow = connection.prepareStatement(
                        "DELETE FROM flows WHERE id = ?")) {
                    deleteFlow.setLong(1, insertedFlowId);
                    deleteFlow.executeUpdate();
                }
            }
        }
    }

    private long insertTestFlow() {
        FlowKey key = new FlowKey("10.0.0.9", 51000, "10.0.0.2", 80, Protocol.TCP);
        Flow flow = new Flow();
        flow.recordPacket(100, Instant.now());
        return flowRepository.insertFlow(key, flow);
    }

    @Test
    void insertAndReadBack_verdictMatches() {
        insertedFlowId = insertTestFlow();

        RuleVerdict blocked = RuleVerdict.block("Source IP 10.0.0.9 is on the block list");
        evaluationRepository.insertEvaluation(insertedFlowId, blocked);

        List<RuleEvaluationRepository.StoredEvaluation> results =
                evaluationRepository.findByFlowId(insertedFlowId);

        assertEquals(1, results.size());
        assertEquals("BLOCK", results.get(0).verdict());
        assertEquals("Source IP 10.0.0.9 is on the block list", results.get(0).reason());
    }

    @Test
    void findByFlowId_returnsEmptyListWhenNoEvaluationsExist() {
        insertedFlowId = insertTestFlow();

        List<RuleEvaluationRepository.StoredEvaluation> results =
                evaluationRepository.findByFlowId(insertedFlowId);

        assertTrue(results.isEmpty());
    }
}