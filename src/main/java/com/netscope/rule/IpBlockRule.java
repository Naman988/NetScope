package com.netscope.rule;

import com.netscope.model.ParsedPacket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link Rule} implementation that blocks packets whose source or
 * destination IP appears in a configured block list.
 *
 * <p>Checks both source and destination IP, since a blocked host
 * could be the origin or the target of traffic. The block list is
 * copied defensively at construction time, so the rule's behavior
 * cannot change after it's built — consistent with the pipeline's
 * general preference for immutable, predictable components.
 *
 * <p>Stateless after construction and thread-safe.
 */
public final class IpBlockRule implements Rule {

    private final Set<String> blockedIps;

    /**
     * @param blockedIps the set of IP addresses to block; must not be null.
     *                    Copied defensively — later changes to the
     *                    caller's set do not affect this rule.
     */
    public IpBlockRule(Set<String> blockedIps) {
        if (blockedIps == null) {
            throw new IllegalArgumentException("blockedIps must not be null");
        }
        this.blockedIps = Collections.unmodifiableSet(new HashSet<>(blockedIps));
    }

    @Override
    public RuleVerdict evaluate(ParsedPacket parsedPacket) {
        if (parsedPacket == null) {
            throw new IllegalArgumentException("parsedPacket must not be null");
        }

        String sourceIp = parsedPacket.getSourceIp();
        String destinationIp = parsedPacket.getDestinationIp();

        if (sourceIp != null && blockedIps.contains(sourceIp)) {
            return RuleVerdict.block("Source IP " + sourceIp + " is on the block list");
        }
        if (destinationIp != null && blockedIps.contains(destinationIp)) {
            return RuleVerdict.block("Destination IP " + destinationIp + " is on the block list");
        }

        return RuleVerdict.allow();
    }
}