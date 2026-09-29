package com.netscope.rule;

import com.netscope.model.ParsedPacket;

import java.util.Collections;
import java.util.List;

/**
 * Evaluates a packet against the active set of rules and returns a
 * single overall verdict.
 *
 * <p>Rules are checked in order; the first rule to block the packet
 * determines the result, and evaluation stops there (short-circuit).
 * If no rule blocks the packet, the overall verdict is allow. This
 * "first block wins" semantics keeps behavior predictable as more
 * rule types are added later.
 */
public final class RuleEngine {

    private final List<Rule> rules;

    /**
     * @param rules the active rule set, evaluated in order; must not be null.
     *              Copied defensively at construction time.
     */
    public RuleEngine(List<Rule> rules) {
        if (rules == null) {
            throw new IllegalArgumentException("rules must not be null");
        }
        this.rules = Collections.unmodifiableList(List.copyOf(rules));
    }

    /**
     * Evaluates the packet against every configured rule, in order,
     * stopping at the first block.
     *
     * @param parsedPacket the packet to evaluate; must not be null
     * @return the overall verdict — the first blocking rule's
     *         verdict, or an allow verdict if none blocked it
     */
    public RuleVerdict evaluate(ParsedPacket parsedPacket) {
        if (parsedPacket == null) {
            throw new IllegalArgumentException("parsedPacket must not be null");
        }

        for (Rule rule : rules) {
            RuleVerdict verdict = rule.evaluate(parsedPacket);
            if (!verdict.isAllowed()) {
                return verdict;
            }
        }

        return RuleVerdict.allow();
    }
}