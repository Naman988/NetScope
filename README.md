# NetScope

> A modular network traffic analysis platform built in Java.

NetScope processes **PCAP files** to analyze network traffic, identify application protocols, track bidirectional flows, evaluate configurable rules, persist results to MySQL, and expose them through a REST API.

## Architecture

```text
PCAP File
   │
   ▼
reader/       → Reads packets using Pcap4J
   │
   ▼
parser/       → Extracts protocols, IPs and ports
   │
   ▼
classifier/   → Identifies application protocols
   │
   ▼
flow/         → Tracks bidirectional flows using 5-tuple
   │
   ▼
rule/         → Evaluates configurable traffic rules
   │
   ▼
persistence/  → Stores results in MySQL via JDBC
   │
   ▼
api/          → Exposes results through Spring Boot REST API
```

Each stage communicates through interfaces, keeping the modules loosely coupled and independently replaceable.

Pcap4J is isolated behind an **Anti-Corruption Layer**, so third-party packet-capture types do not leak into the rest of the application.

## Key Features

* PCAP packet ingestion
* Protocol, IP and port parsing
* Application-layer protocol classification
* Bidirectional flow tracking
* Packet, byte and timestamp statistics
* Configurable allow/block rules
* MySQL persistence using raw JDBC
* Spring Boot REST API
* JUnit 5 tests
* End-to-end verification with a real PCAP file

## Tech Stack

* **Java 21**
* **Maven**
* **Pcap4J**
* **Spring Boot**
* **MySQL**
* **JDBC**
* **JUnit 5**
* **SLF4J + Logback**

## Project Structure

```text
src/main/java/com/netscope/
├── reader/
├── parser/
├── classifier/
├── flow/
├── rule/
├── persistence/
├── api/
└── PipelineRunner.java
```

## Setup

### 1. Create the database

```sql
CREATE DATABASE netscope;
```

Run `schema.sql` against the database.

### 2. Create an application user

```sql
CREATE USER 'netscope_app'@'localhost'
IDENTIFIED BY 'your_password_here';

GRANT ALL PRIVILEGES ON netscope.* TO 'netscope_app'@'localhost';

FLUSH PRIVILEGES;
```

### 3. Configure the application

Copy:

```text
src/main/resources/application.properties.example
```

to:

```text
src/main/resources/application.properties
```

and add your database credentials.

### 4. Run the ingestion pipeline

```bash
mvn exec:java \
  -Dexec.mainClass="com.netscope.PipelineRunner" \
  -Dexec.args="src/test/resources/sample.pcap"
```

### 5. Start the API

```bash
mvn spring-boot:run
```

## API

### Get all flows

```http
GET /flows
```

### Get a flow by ID

```http
GET /flows/{id}
```

The second endpoint also returns the rule evaluations associated with the flow.

## Testing

```bash
mvn test
```

The pipeline has been verified end-to-end against a real PCAP capture file.

## Design Highlights

* **Interface-driven architecture** for low coupling and easier testing
* **Anti-Corruption Layer** around Pcap4J
* **5-tuple based flow tracking**
* **Raw JDBC persistence** for explicit database interaction
* Clear separation between packet processing, business logic, persistence, and API layers

## Status

✅ **Complete**

Implemented and verified:

* Packet reader
* Packet parser
* Protocol classifier
* Flow tracking
* Rule engine
* MySQL/JDBC persistence
* Spring Boot REST API
* Automated tests



## License

MIT License
