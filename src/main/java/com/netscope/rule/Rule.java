package com.netscope.rule;

import com.netscope.model.ParsedPacket;

/**
 * Abstraction over a single filtering rule.
 *
 * <p>As with the reader/parser/classifier packages, downstream code
 * depends only on this interface, not on a specific rule
 * implementation. Today the only implementation is IP-based
 * allow/block; a future implementation (port-based, protocol-based,
 * rate-based) can be added without {@link RuleEngine} changing.
 */
public interface Rule {

    /**
     * Evaluates a single packet against this rule.
     *
     * @param parsedPacket the packet to evaluate; must not be null
     * @return this rule's verdict for the packet, never null
     */
    RuleVerdict evaluate(ParsedPacket parsedPacket);
}