package com.netscope.rule;

import com.netscope.model.PacketMetadata;
import com.netscope.model.ParsedPacket;
import com.netscope.model.Protocol;
import com.netscope.model.RawPacket;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RuleEngineTest {

    private ParsedPacket packetBetween(String sourceIp, String destinationIp) {
        RawPacket rawPacket = new RawPacket(
                new byte[]{1}, new PacketMetadata(Instant.now(), 1, 1, null));
        return new ParsedPacket(rawPacket, Protocol.TCP, sourceIp, destinationIp, 51000, 80);
    }

    @Test
    void evaluate_returnsAllowWhenNoRulesBlock() {
        RuleEngine engine = new RuleEngine(List.of(new IpBlockRule(Set.of("192.168.1.1"))));
        RuleVerdict verdict = engine.evaluate(packetBetween("10.0.0.1", "10.0.0.2"));

        assertTrue(verdict.isAllowed());
    }

    @Test
    void evaluate_returnsBlockFromFirstMatchingRule() {
        RuleEngine engine = new RuleEngine(List.of(new IpBlockRule(Set.of("10.0.0.9"))));
        RuleVerdict verdict = engine.evaluate(packetBetween("10.0.0.9", "10.0.0.2"));

        assertFalse(verdict.isAllowed());
    }

    @Test
    void evaluate_withEmptyRuleSet_alwaysAllows() {
        RuleEngine engine = new RuleEngine(List.of());
        RuleVerdict verdict = engine.evaluate(packetBetween("10.0.0.9", "10.0.0.2"));

        assertTrue(verdict.isAllowed());
    }
}