package com.netscope.rule;

/**
 * Immutable result of evaluating a packet against a single rule or
 * the full rule set: whether it is allowed or blocked, and why.
 *
 * <p>Kept as its own type rather than a raw boolean so that a
 * blocked packet always carries a human-readable reason — useful
 * for logging and, later, for the rule_evaluations table Sprint 7
 * persists.
 */
public final class RuleVerdict {

    private final boolean allowed;
    private final String reason;

    private RuleVerdict(boolean allowed, String reason) {
        this.allowed = allowed;
        this.reason = reason;
    }

    /**
     * @return an "allow" verdict with no specific reason (nothing blocked it)
     */
    public static RuleVerdict allow() {
        return new RuleVerdict(true, "No matching block rule");
    }

    /**
     * @param reason human-readable explanation of why the packet was blocked; must not be null
     * @return a "block" verdict carrying the given reason
     */
    public static RuleVerdict block(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be null or blank for a block verdict");
        }
        return new RuleVerdict(false, reason);
    }

    /** @return true if the packet is allowed, false if blocked */
    public boolean isAllowed() {
        return allowed;
    }

    /** @return the reason for this verdict — always present, even for allow */
    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return (allowed ? "ALLOW" : "BLOCK") + " (" + reason + ")";
    }
}