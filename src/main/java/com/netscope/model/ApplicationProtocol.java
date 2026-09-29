package com.netscope.model;

/**
 * Represents the Layer 7 (application) protocol inferred for a
 * packet, as distinct from {@link Protocol}, which covers Layers
 * 2 through 4 only.
 *
 * <p>This separation mirrors the pipeline's own separation of
 * concerns: {@link Protocol} is determined by the parser from the
 * packet's actual header bytes, while {@code ApplicationProtocol}
 * is inferred by the classifier from higher-level signals (today,
 * well-known port numbers).
 *
 * <p>UNKNOWN is the defensive default, consistent with how
 * {@link Protocol#UNKNOWN} is used elsewhere in the pipeline —
 * classification never fails outright, it degrades to UNKNOWN.
 */
public enum ApplicationProtocol {

    HTTP("HTTP"),
    HTTPS("HTTPS"),
    DNS("DNS"),
    UNKNOWN("Unknown");

    private final String displayName;

    ApplicationProtocol(String displayName) {
        this.displayName = displayName;
    }

    /** @return human-readable name suitable for reports and logs */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}