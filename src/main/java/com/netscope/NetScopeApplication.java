package com.netscope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point. Starts the web server and REST API only —
 * it does not run the packet-capture pipeline. Run
 * {@link PipelineRunner} separately first to populate the database.
 */
@SpringBootApplication
public class NetScopeApplication {
    public static void main(String[] args) {
        SpringApplication.run(NetScopeApplication.class, args);
    }
}