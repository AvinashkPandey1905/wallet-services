# Wallet Service

A high-performance implementation of a Wallet Service backend using Java 17, Spring Boot 3, and PostgreSQL. Designed to handle **1000 RPS per wallet** with strict data consistency using pessimistic locking.

## Features

*   **Robust Money Transfer**: Supports `DEPOSIT` and `WITHDRAW` operations.
*   **High Concurrency**: Handles 1000+ Requests Per Second (RPS) per wallet without race conditions (`PESSIMISTIC_WRITE` locking).
*   **Data Integrity**: Transactional operations ensure balances are always accurate.
*   **Containerized**: Fully Dockerized (App + PostgreSQL) for easy deployment.
*   **Database Migrations**: Liquibase managed schema.

## Tech Stack

*   **Language**: Java 17
*   **Framework**: Spring Boot 3
*   **Database**: PostgreSQL 15
*   **Build Tool**: Maven
*   **Migrations**: Liquibase
*   **Testing**: JUnit 5, MockMvc, H2 (In-Memory)

## Getting Started

### Prerequisites

*   Docker & Docker Compose

### Running the Application

1.  Clone the repository:
    ```bash
    git clone https://github.com/AvinashkPandey1905/-wallet-service.git
    cd -wallet-service
    ```

2.  Start verification environment:
    ```bash
    docker-compose up --build
    ```

The application will be available at **`http://localhost:8081`**.

## API Reference

**Base URL**: `http://localhost:8081/api/v1`

### 1. Deposit Funds
*   **Endpoint**: `POST /wallet`
*   **Description**: Add funds to a wallet.
*   **Example**:
    ```bash
    curl -i -X POST http://localhost:8081/api/v1/wallet \
    -H "Content-Type: application/json" \
    -d '{
      "valletId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "operationType": "DEPOSIT",
      "amount": 1000
    }'
    ```

### 2. Withdraw Funds
*   **Endpoint**: `POST /wallet`
*   **Description**: Deduct funds from a wallet. Returns `400 Bad Request` if insufficient funds.
*   **Example**:
    ```bash
    curl -i -X POST http://localhost:8081/api/v1/wallet \
    -H "Content-Type: application/json" \
    -d '{
      "valletId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "operationType": "WITHDRAW",
      "amount": 500
    }'
    ```

### 3. Check Balance
*   **Endpoint**: `GET /wallets/{walletId}`
*   **Description**: Get current balance.
*   **Example**:
    ```bash
    curl http://localhost:8081/api/v1/wallets/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11
    ```

## Testing

Run integration and concurrency tests:

```bash
./mvnw test
```

Includes `WalletIntegrationTest` which verifies:
*   Functional correctness (Deposit/Withdraw).
*   **Concurrency**: 100 threads hitting the same wallet simultaneously.
*   **Error Handling**: Validates `400`, `404`, and `429` (Too Many Requests) responses.

## Architecture Highlights

*   **Pessimistic Locking**: Uses `SELECT ... FOR UPDATE` via JPA `@Lock(LockModeType.PESSIMISTIC_WRITE)` to serialize access to wallet rows during modification. This prevents lost updates.
*   **Connection Pooling**: HikariCP is configured with appropriate timeouts and pool size to handle burst traffic.
*   **Error Handling**:
    *   `405 Method Not Allowed`: For using GET on POST-only endpoints.
    *   `429 Too Many Requests`: If database lock acquisition fails under extreme load.
