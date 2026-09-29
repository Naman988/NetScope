package com.netscope.rule;

import com.netscope.model.PacketMetadata;
import com.netscope.model.ParsedPacket;
import com.netscope.model.Protocol;
import com.netscope.model.RawPacket;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IpBlockRuleTest {

    private ParsedPacket packetBetween(String sourceIp, String destinationIp) {
        RawPacket rawPacket = new RawPacket(
                new byte[]{1}, new PacketMetadata(Instant.now(), 1, 1, null));
        return new ParsedPacket(rawPacket, Protocol.TCP, sourceIp, destinationIp, 51000, 80);
    }

    @Test
    void evaluate_blocksWhenSourceIpIsOnList() {
        IpBlockRule rule = new IpBlockRule(Set.of("10.0.0.9"));
        RuleVerdict verdict = rule.evaluate(packetBetween("10.0.0.9", "10.0.0.2"));

        assertFalse(verdict.isAllowed());
        assertTrue(verdict.getReason().contains("10.0.0.9"));
    }

    @Test
    void evaluate_blocksWhenDestinationIpIsOnList() {
        IpBlockRule rule = new IpBlockRule(Set.of("10.0.0.9"));
        RuleVerdict verdict = rule.evaluate(packetBetween("10.0.0.2", "10.0.0.9"));

        assertFalse(verdict.isAllowed());
    }

    @Test
    void evaluate_allowsWhenNeitherIpIsOnList() {
        IpBlockRule rule = new IpBlockRule(Set.of("10.0.0.9"));
        RuleVerdict verdict = rule.evaluate(packetBetween("10.0.0.1", "10.0.0.2"));

        assertTrue(verdict.isAllowed());
    }

    @Test
    void constructor_defensivelyCopiesBlockList() {
        java.util.Set<String> mutableSet = new java.util.HashSet<>(Set.of("10.0.0.9"));
        IpBlockRule rule = new IpBlockRule(mutableSet);

        mutableSet.clear(); // mutate the original after construction

        RuleVerdict verdict = rule.evaluate(packetBetween("10.0.0.9", "10.0.0.2"));
        assertFalse(verdict.isAllowed(), "Rule should still block based on the set at construction time");
    }
}