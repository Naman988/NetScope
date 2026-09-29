package com.netscope.flow;

import com.netscope.model.Protocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlowKeyTest {

    @Test
    void sameConversationInBothDirections_producesEqualKeys() {
        FlowKey requestDirection = new FlowKey("10.0.0.1", 51000, "10.0.0.2", 80, Protocol.TCP);
        FlowKey responseDirection = new FlowKey("10.0.0.2", 80, "10.0.0.1", 51000, Protocol.TCP);

        assertEquals(requestDirection, responseDirection);
        assertEquals(requestDirection.hashCode(), responseDirection.hashCode());
    }

    @Test
    void differentPorts_produceDifferentKeys() {
        FlowKey a = new FlowKey("10.0.0.1", 51000, "10.0.0.2", 80, Protocol.TCP);
        FlowKey b = new FlowKey("10.0.0.1", 51001, "10.0.0.2", 80, Protocol.TCP);

        assertNotEquals(a, b);
    }

    @Test
    void differentProtocols_produceDifferentKeysEvenWithSameEndpoints() {
        FlowKey tcp = new FlowKey("10.0.0.1", 51000, "10.0.0.2", 80, Protocol.TCP);
        FlowKey udp = new FlowKey("10.0.0.1", 51000, "10.0.0.2", 80, Protocol.UDP);

        assertNotEquals(tcp, udp);
    }

    @Test
    void canBeUsedAsHashMapKey_acrossBothDirections() {
        java.util.Map<FlowKey, String> map = new java.util.HashMap<>();
        map.put(new FlowKey("10.0.0.1", 51000, "10.0.0.2", 80, Protocol.TCP), "first insert");

        String result = map.get(new FlowKey("10.0.0.2", 80, "10.0.0.1", 51000, Protocol.TCP));
        assertEquals("first insert", result);
    }
}