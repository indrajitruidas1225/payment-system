# Payment Processing System

A production-style payment processing backend built using:

* Java
* Spring Boot
* PostgreSQL
* Redis
* Apache Kafka
* Docker

This project was built step-by-step to understand how real-world payment systems handle:

* concurrent transactions
* duplicate requests
* retries
* async processing
* event-driven architecture
* payment lifecycle tracking
* ledger-based accounting
* failure handling

---

# Architecture Overview

![Image](https://images.openai.com/static-rsc-4/suLN4MbIf5Mrt7m52azK0oM3ASY0tOlt6ymWSaEvdQdiYd7UweF-5NerXksW1eXily206RbFqmgblurnww6iiWpodmmgACakct7qu_po1KpRh_tCtmBSQNEhnJ9lQSiuKJsMR9g8BHa4Bgza3YBmiD0MGe368CI9WbyyCWdZeq_AP3cyX_SCVVJ6KvBe7n9h?purpose=fullsize)


```text
Client
   ↓
Spring Boot API
   ↓
Redis (Idempotency Check)
   ↓
Kafka Topic
   ↓
Kafka Consumer
   ↓
PostgreSQL
```

---

# Features Implemented

## 1. Account Management

* Sender and receiver accounts
* Balance updates
* Database persistence using PostgreSQL

---

## 2. ACID Transactions

Used:

```java
@Transactional
```

to ensure:

* atomic balance updates
* consistent DB state
* rollback on failures

---

## 3. Concurrency Control

Implemented pessimistic locking:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

This prevents:

* double spending
* race conditions
* concurrent balance corruption

Equivalent SQL behavior:

```sql
SELECT * FROM accounts
FOR UPDATE;
```

---

## 4. Redis-Based Idempotency

Implemented duplicate request protection using Redis.

Flow:

* every request contains an `idempotencyKey`
* Redis `SETNX` (`setIfAbsent`) ensures same request is processed only once

Example:

```java
redisTemplate.opsForValue().setIfAbsent(...)
```

Prevents:

* duplicate payments
* retry-based double deduction
* accidental reprocessing

---

## 5. Kafka-Based Async Processing

Instead of:

```text
API → DB
```

system uses:

```text
API → Kafka → Consumer → DB
```

Benefits:

* async processing
* traffic buffering
* scalable architecture
* retry capability
* decoupled services

---

## 6. Payment Lifecycle Tracking

Payment statuses implemented:

* PROCESSING
* SUCCESS
* FAILED
* REVERSED

This enables:

* payment history
* status APIs
* reconciliation
* async visibility

---

## 7. Immutable Ledger

Implemented ledger entries for all money movement.

Ledger entry types:

* DEBIT
* CREDIT
* REVERSAL

Benefits:

* financial auditability
* transaction history
* debugging
* reconciliation

Balances are mutable.
Ledger entries are append-only.

---

## 8. Saga-Style Compensation

Implemented compensation logic for partial failures.

Scenario:

* sender debited
* receiver step fails

Compensation:

* sender refunded
* reversal ledger entry created
* payment marked REVERSED

This demonstrates distributed systems compensation concepts.

---

## 9. Kafka Retry Handling

Observed and handled:

* Kafka offset retries
* consumer failure behavior
* at-least-once delivery semantics

Learned how Kafka retries messages when consumer processing fails.

---

## 10. Dead Letter Queue (DLQ)

Implemented DLQ support using Spring Kafka error handling.

Flow:

```text
Consumer Failure
    ↓
Retry 3 times
    ↓
Move to payment-dlq
```

Benefits:

* poison message isolation
* prevents infinite retries
* failure inspection
* operational recovery

---

# Tech Stack

| Technology    | Purpose               |
| ------------- | --------------------- |
| Spring Boot   | Backend framework     |
| PostgreSQL    | Primary database      |
| Redis         | Idempotency           |
| Kafka         | Event streaming       |
| Docker        | Infrastructure setup  |
| JPA/Hibernate | ORM                   |
| Maven         | Dependency management |

---

# Project Structure

```text
src/main/java/com/service/payment_system

├── config
├── consumer
├── controller
├── dto
├── entity
├── event
├── producer
├── repository
└── service
```

---

# PostgreSQL Setup

PostgreSQL is required.

## Create Database

```sql
CREATE DATABASE payment_system;
```

---

## application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payment_system
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

# Redis Setup

Using Docker:

```bash
docker run -d --name redis -p 6379:6379 redis
```

---

# Kafka Setup

Create `docker-compose.yml`

```yaml
version: '3'

services:

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:latest
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

Run:

```bash
docker-compose up -d
```

---

# Kafka Configuration

```properties
spring.kafka.bootstrap-servers=localhost:9092

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

spring.kafka.consumer.group-id=payment-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer

spring.kafka.consumer.properties.spring.json.trusted.packages=*
```

---

# API Example

## Create Payment

```http
POST /payments
```

Request:

```json
{
  "idempotencyKey":"txn_1001",
  "senderId":1,
  "receiverId":2,
  "amount":100
}
```

Response:

```text
Payment Initiated
```

---

# Important Concepts Learned

This project focuses heavily on failure handling and distributed systems thinking.

Concepts covered:

* idempotency
* distributed retries
* Kafka offsets
* transactional consistency
* compensation logic
* event-driven systems
* async workflows
* concurrency control
* ledger accounting
* DLQ handling

---

# Current Limitations

This is still a single-service architecture.

Not yet implemented:

* microservice separation
* distributed saga orchestration
* outbox pattern
* read replicas
* CQRS
* fraud detection
* observability/tracing
* rate limiting

---

# Future Improvements

Possible next steps:

* Outbox Pattern
* CQRS
* Read Replicas
* Notification Service
* Fraud Detection Service
* Metrics & Monitoring
* Kubernetes Deployment
* API Gateway
* Distributed Saga Orchestrator

---

# Learning Outcome

The main goal of this project was not just building APIs, but understanding:

```text
What happens when systems fail?
```

This includes:

* retries
* partial failures
* duplicate requests
* rollback vs compensation
* async consistency
* event durability

These are core backend engineering and system design concepts used in large-scale payment systems.
