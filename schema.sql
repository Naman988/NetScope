-- NetScope database schema
-- Run this against a MySQL 8.x server to create the tables NetScope persists to.
--
-- Setup:
--   1. CREATE DATABASE netscope;
--   2. USE netscope;
--   3. Run this file.
--
-- See README.md for the scoped application-user setup used in development.

CREATE TABLE IF NOT EXISTS flows (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     endpoint_a_ip VARCHAR(45) NOT NULL,
    endpoint_a_port INT NOT NULL,
    endpoint_b_ip VARCHAR(45) NOT NULL,
    endpoint_b_port INT NOT NULL,
    protocol VARCHAR(20) NOT NULL,
    packet_count BIGINT NOT NULL,
    byte_count BIGINT NOT NULL,
    first_seen TIMESTAMP(3) NOT NULL,
    last_seen TIMESTAMP(3) NOT NULL,
    UNIQUE KEY uq_flow_tuple (endpoint_a_ip, endpoint_a_port, endpoint_b_ip, endpoint_b_port, protocol)
    );

CREATE TABLE IF NOT EXISTS rule_evaluations (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                flow_id BIGINT NOT NULL,
                                                verdict VARCHAR(10) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    evaluated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    FOREIGN KEY (flow_id) REFERENCES flows(id)
    );