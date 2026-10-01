package com.netscope;

import com.netscope.classifier.PortBasedTrafficClassifier;
import com.netscope.flow.Flow;
import com.netscope.flow.FlowKey;
import com.netscope.flow.FlowTracker;
import com.netscope.model.ParsedPacket;
import com.netscope.model.RawPacket;
import com.netscope.parser.DefaultPacketParser;
import com.netscope.persistence.DataSourceProvider;
import com.netscope.persistence.FlowRepository;
import com.netscope.persistence.RuleEvaluationRepository;
import com.netscope.reader.PcapFileReader;
import com.netscope.rule.IpBlockRule;
import com.netscope.rule.RuleEngine;
import com.netscope.rule.RuleVerdict;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Standalone entry point that runs a {@code .pcap} file through the
 * full NetScope pipeline exactly once: read, parse, classify, track
 * flows, evaluate rules, and persist the result to MySQL.
 *
 * <p>Deliberately not a Spring bean — this is a one-shot ingestion
 * script, run manually and separately from {@link NetScopeApplication},
 * which only serves whatever this run already persisted.
 */
public final class PipelineRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: PipelineRunner <path-to-pcap-file>");
            System.exit(1);
        }

        String pcapPath = args[0];

        // 1. Read
        List<RawPacket> rawPackets = new PcapFileReader(pcapPath).readPackets();
        System.out.println("Read " + rawPackets.size() + " packets");

        // 2. Parse
        DefaultPacketParser parser = new DefaultPacketParser();

        // 3. Classify (constructed here; classification result isn't persisted
        //    in this sprint's schema, but resolved per packet for completeness)
        PortBasedTrafficClassifier classifier = new PortBasedTrafficClassifier();

        // 4. Track flows
        FlowTracker flowTracker = new FlowTracker();

        // 5. Rule engine — example block list; adjust as needed
        RuleEngine ruleEngine = new RuleEngine(List.of(new IpBlockRule(Set.of())));

        Map<FlowKey, RuleVerdict> latestVerdictByFlow = new java.util.HashMap<>();

        for (RawPacket rawPacket : rawPackets) {
            ParsedPacket parsedPacket = parser.parse(rawPacket);
            classifier.classify(parsedPacket); // resolved, not yet persisted

            boolean tracked = flowTracker.recordPacket(parsedPacket);
            if (!tracked) {
                continue; // no ports (ICMP/ARP/etc.) — nothing to persist as a flow
            }

            RuleVerdict verdict = ruleEngine.evaluate(parsedPacket);

            FlowKey key = new FlowKey(
                    parsedPacket.getSourceIp(), parsedPacket.getSourcePort(),
                    parsedPacket.getDestinationIp(), parsedPacket.getDestinationPort(),
                    parsedPacket.getProtocol());
            latestVerdictByFlow.put(key, verdict);
        }

        // 6. Persist
        DataSourceProvider dataSourceProvider = new DataSourceProvider();
        FlowRepository flowRepository = new FlowRepository(dataSourceProvider);
        RuleEvaluationRepository evaluationRepository = new RuleEvaluationRepository(dataSourceProvider);



        for (Map.Entry<FlowKey, RuleVerdict> entry : latestVerdictByFlow.entrySet()) {
            FlowKey key = entry.getKey();
            Flow flow = flowTracker.getFlow(key);
            long flowId = flowRepository.insertFlow(key, flow);
            evaluationRepository.insertEvaluation(flowId, entry.getValue());
        }

        System.out.println("Persisted " + latestVerdictByFlow.size() + " flows with rule evaluations.");
    }
}