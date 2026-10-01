package com.netscope.api;

import com.netscope.persistence.FlowRepository.StoredFlow;

import java.time.Instant;
import java.util.List;

/**
 * JSON-facing representation of a flow, optionally including its
 * rule evaluations.
 */
public record FlowResponse(
        long id,
        String endpointAIp,
        int endpointAPort,
        String endpointBIp,
        int endpointBPort,
        String protocol,
        long packetCount,
        long byteCount,
        Instant firstSeen,
        Instant lastSeen,
        List<RuleEvaluationResponse> ruleEvaluations
) {
    public static FlowResponse from(StoredFlow stored, List<RuleEvaluationResponse> evaluations) {
        return new FlowResponse(
                stored.id(),
                stored.endpointAIp(),
                stored.endpointAPort(),
                stored.endpointBIp(),
                stored.endpointBPort(),
                stored.protocol().name(),
                stored.packetCount(),
                stored.byteCount(),
                stored.firstSeen(),
                stored.lastSeen(),
                evaluations
        );
    }
}