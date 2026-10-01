package com.netscope.api;

import com.netscope.persistence.DataSourceProvider;
import com.netscope.persistence.FlowRepository;
import com.netscope.persistence.RuleEvaluationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer sitting between {@link FlowController} and the
 * persistence repositories — assembles a {@link FlowResponse}
 * (including its rule evaluations) from the two underlying
 * repositories' independent lookups.
 *
 * <p>{@code FlowRepository} and {@code RuleEvaluationRepository}
 * are plain classes (not Spring components) constructed here with
 * their own {@link DataSourceProvider}, consistent with keeping the
 * persistence package framework-agnostic — Spring only wraps it at
 * this boundary.
 */
@Service
public class FlowService {

    private final FlowRepository flowRepository;
    private final RuleEvaluationRepository ruleEvaluationRepository;

    public FlowService() {
        DataSourceProvider dataSourceProvider = new DataSourceProvider();
        this.flowRepository = new FlowRepository(dataSourceProvider);
        this.ruleEvaluationRepository = new RuleEvaluationRepository(dataSourceProvider);
    }

    /**
     * Fetches a single flow by ID, including its rule evaluations.
     *
     * @param flowId the flow's database ID
     * @return the assembled response, or empty if no such flow exists
     */
    public Optional<FlowResponse> getFlow(long flowId) {
        return flowRepository.findById(flowId).map(storedFlow -> {
            List<RuleEvaluationResponse> evaluations = ruleEvaluationRepository
                    .findByFlowId(flowId).stream()
                    .map(RuleEvaluationResponse::from)
                    .toList();
            return FlowResponse.from(storedFlow, evaluations);
        });
    }

    /** @return all flows, without their rule evaluations (use {@link #getFlow} for that detail) */
    public List<FlowResponse> getAllFlows() {
        return flowRepository.findAll().stream()
                .map(stored -> FlowResponse.from(stored, List.of()))
                .toList();
    }
}