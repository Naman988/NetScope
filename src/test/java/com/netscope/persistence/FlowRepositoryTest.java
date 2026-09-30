package com.netscope.persistence;

import com.netscope.flow.Flow;
import com.netscope.flow.FlowKey;
import com.netscope.model.Protocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FlowRepositoryTest {

    private final DataSourceProvider dataSourceProvider = new DataSourceProvider();
    private final FlowRepository repository = new FlowRepository(dataSourceProvider);

    private Long insertedFlowId;

    @AfterEach
    void cleanUp() throws Exception {
        if (insertedFlowId != null) {
            try (Connection connection = dataSourceProvider.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM flows WHERE id = ?")) {
                statement.setLong(1, insertedFlowId);
                statement.executeUpdate();
            }
        }
    }

    @Test
    void insertAndReadBack_fieldsMatch() {
        FlowKey key = new FlowKey("10.0.0.1", 51000, "10.0.0.2", 80, Protocol.TCP);

        Flow flow = new Flow();
        Instant now = Instant.now();
        flow.recordPacket(150, now);
        flow.recordPacket(300, now.plusSeconds(1));

        insertedFlowId = repository.insertFlow(key, flow);
        assertTrue(insertedFlowId > 0);

        Optional<FlowRepository.StoredFlow> stored = repository.findById(insertedFlowId);

        assertTrue(stored.isPresent());
        FlowRepository.StoredFlow result = stored.get();

        assertEquals("10.0.0.1", result.endpointAIp());
        assertEquals(51000, result.endpointAPort());
        assertEquals("10.0.0.2", result.endpointBIp());
        assertEquals(80, result.endpointBPort());
        assertEquals(Protocol.TCP, result.protocol());
        assertEquals(2, result.packetCount());
        assertEquals(450, result.byteCount());
    }

    @Test
    void findById_returnsEmptyForNonexistentId() {
        Optional<FlowRepository.StoredFlow> result = repository.findById(-1L);
        assertTrue(result.isEmpty());
    }
}