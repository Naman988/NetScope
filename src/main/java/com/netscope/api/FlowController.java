package com.netscope.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only REST API exposing persisted flow analysis results.
 *
 * <p>This controller does not trigger packet capture or analysis —
 * it only serves data already written to MySQL by {@link com.netscope.PipelineRunner}.
 * The two concerns are deliberately decoupled: running the pipeline
 * and serving its results are separate entry points.
 */
@RestController
public class FlowController {

    private final FlowService flowService;

    public FlowController(FlowService flowService) {
        this.flowService = flowService;
    }

    /** GET /flows — lists all persisted flows (without rule evaluation detail). */
    @GetMapping("/flows")
    public List<FlowResponse> listFlows() {
        return flowService.getAllFlows();
    }

    /** GET /flows/{id} — a single flow, including its rule evaluations. */
    @GetMapping("/flows/{id}")
    public ResponseEntity<FlowResponse> getFlow(@PathVariable("id") long id) {
        return flowService.getFlow(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}