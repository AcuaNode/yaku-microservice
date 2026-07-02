# YAKU Microservices System

Welcome to the **YAKU** backend ecosystem. This repository contains the complete microservices architecture for the YAKU platform, fully containerized and orchestration-ready.

---

## 1. Architecture Overview

The system uses a decentralized microservices architecture running inside a dedicated Docker network. Communications flow through the API Gateway, and asynchronous events are distributed via Apache Kafka.

```mermaid
graph TD
    Client[Frontend / Client] -->|API Calls (Port 8080)| Gateway[API Gateway (contains IAM)]
    Gateway -->|Internal HTTP| Equipment[Equipment Service]
    Gateway -->|Internal HTTP| Telemetry[Telemetry Service]
    Gateway -->|Internal HTTP| Notification[Notification Service]
    Gateway -->|Internal HTTP| Subscription[Subscription Service]
    
    Gateway -->|Events| Kafka[Kafka Message Bus]
    Equipment -->|Events| Kafka
    Telemetry -->|Events| Kafka
    Notification -->|Events| Kafka
    Subscription -->|Events| Kafka

    Gateway -->|JPA| IAM_DB[(IAM PostgreSQL DB)]
    Equipment -->|JPA| Equip_DB[(Equipment PostgreSQL DB)]
    Telemetry -->|JPA| Tele_DB[(Telemetry PostgreSQL DB)]
    Notification -->|JPA| Notif_DB[(Notification PostgreSQL DB)]
    Subscription -->|JPA| Sub_DB[(Subscription PostgreSQL DB)]
```

---

## 2. Services and Ports Mapping

All services connect internally inside the Docker network `yaku-network`. Port `8080` is standard for all microservice containers internally, whereas different host ports are exposed to prevent conflicts during local developer runs:

| Service Name | Host Port | Container Port | Database Name | Exposed DB Host Port | DB Container Port |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `api-gateway` | `8080` | `8080` | `yaku_iam` | `5434` | `5432` |
| `equipment-service` | `8081` | `8080` | `yaku_equipment` | `5432` | `5432` |
| `telemetry-service` | `8082` | `8080` | `yaku_telemetry` | `5436` | `5432` |
| `notification-service` | `8083` | `8080` | `yaku_notifications` | `5435` | `5432` |
| `subscription-service` | `8084` | `8080` | `mydatabase` | `5433` | `5432` |
| `kafka` | `9092` | `9092` | N/A | `9092` | `9092` |

---

## 3. Project Structure

```text
YAKU/
├── compose.yaml                          # Root Docker Compose Orchestration File
├── README.md                             # Architecture & Operations Guide
├── yaku-apigateway/                     # API Gateway (with consolidated IAM logic)
│   ├── Dockerfile
│   └── src/main/resources/application.yml
├── yaku-equipment-microservice/          # Equipment Microservice
│   ├── Dockerfile
│   └── src/main/resources/application-dev.properties
├── yaku-notification-microservice/       # Notification Microservice
│   ├── Dockerfile
│   └── src/main/resources/application-dev.properties
├── yaku-subscription-microservice/       # Subscription Microservice
│   ├── Dockerfile
│   └── src/main/resources/application-dev.properties
└── yaku-telemetry-microservice/          # Telemetry Microservice
    ├── Dockerfile
    └── src/main/resources/application-dev.properties
```

---

## 4. Building and Running

### Prerequisites
- Docker Installed (Desktop or Engine)
- Compose plugin installed

### Run the System
To build all images and start all database containers, Kafka broker, and microservices, simply execute:
```bash
docker compose up --build
```

### Stop the System
To stop all services and keep the database volumes intact:
```bash
docker compose down
```

To stop all services and **delete all database volumes** (starting from a clean slate):
```bash
docker compose down -v
```

---

## 5. Environment Variables & Configurations
The system uses environment variables with fallback default configurations, meaning the project runs out-of-the-box inside Docker Compose with **zero manual configuration** required. 

However, you can override any parameter in `compose.yaml` or by passing host env vars:
- `SPRING_DATASOURCE_URL`: Custom database connection string.
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Custom Kafka bootstrap address.
- `STRIPE_SECRET_KEY` / `STRIPE_WEBHOOK_SECRET`: Keys for Stripe payment integrations.

---

## 6. Troubleshooting

- **Databases take long to initialize**: The microservices use `depends_on` with `condition: service_healthy` check using `pg_isready` and wait for the databases to accept connections before launching.
- **Port Conflict**: If port 8080-8084 is already in use by other services on your host, stop them or change the host-port mapping (left side of `ports:` in `compose.yaml`) before booting.
- **Kafka connection refused**: Ensure `yaku-kafka` container is healthy. The services wait for the Kafka healthcheck to complete.
