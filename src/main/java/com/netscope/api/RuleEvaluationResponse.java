package com.netscope.api;

import com.netscope.persistence.RuleEvaluationRepository.StoredEvaluation;

import java.time.Instant;

/**
 * JSON-facing representation of a single rule evaluation.
 *
 * <p>Kept separate from {@link StoredEvaluation} so the API's public
 * shape can evolve independently of the persistence layer's internal
 * record — a deliberate seam, same reasoning as keeping
 * {@code ParsedPacket} separate from {@code RawPacket}.
 */
public record RuleEvaluationResponse(
        long id,
        String verdict,
        String reason,
        Instant evaluatedAt
) {
    public static RuleEvaluationResponse from(StoredEvaluation stored) {
        return new RuleEvaluationResponse(
                stored.id(), stored.verdict(), stored.reason(), stored.evaluatedAt());
    }
}