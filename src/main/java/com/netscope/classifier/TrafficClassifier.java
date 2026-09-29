package com.netscope.classifier;

import com.netscope.model.ApplicationProtocol;
import com.netscope.model.ParsedPacket;

/**
 * Abstraction over "something that infers an application-layer
 * protocol for a parsed packet."
 *
 * <p>As with {@link com.netscope.reader.PacketReader} and
 * {@link com.netscope.parser.PacketParser}, this interface exists
 * so downstream packages (flow tracker, rule engine) depend only
 * on this contract, not on a specific classification strategy.
 * Today the only implementation is port-based; a future
 * implementation could add deep packet inspection without any
 * downstream code changing.
 */
public interface TrafficClassifier {

    /**
     * Infers the application-layer protocol for a parsed packet.
     *
     * <p>This method never throws for an unrecognized port or
     * protocol — it returns {@link ApplicationProtocol#UNKNOWN} in
     * that case, consistent with the pipeline's convention of never
     * failing on ordinary, if uninteresting, traffic.
     *
     * @param parsedPacket the packet to classify; must not be null
     * @return the inferred application protocol, never null
     */
    ApplicationProtocol classify(ParsedPacket parsedPacket);
}