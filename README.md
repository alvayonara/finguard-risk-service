# finguard-risk-service

Backend system designed to simulate financial user evaluation (simplified risk) and monitoring flows.

The project is part of personal portfolio project focuses on the following:
- Modular design
- Event-driven processing
- Containerized deployment
- CI/CD automation

---

## Features Implemented

- JWT-based authentication + token refresh flow
- Modularization modules + Maven
- Event-driven using Kafka
- MySQL integration db
- Dockerized env
- CI/CD pipeline + image versioning

---

## What's Inside?

**Backend:** Java 21, Spring Boot 3.3.5, Spring WebFlux, Spring Security (JWT)

**Database:** MySQL 8

**Messaging:** Apache Kafka

**Reverse Proxy:** Nginx

**Security:** Cloudflare (DNS, SSL, basic protection)

**Hosting:** Virtual Machine (VM) – niagahoster.com

**CI/CD:** Jenkins (self-hosted)

**Containerization:** Dockerfile & Docker Compose

---

## Project Structure

Split into 4 modules:

```
finguard-service/
├── finguard-common/      # Shared config, security, utilities
├── finguard-core/       # Core services and domain logic
├── finguard-risk-engine/      # Financial evaluation and processing logic
└── finguard-app/         # Application entry point
```

## License

Copyright (c) 2026 Alva Yonara
All rights reserved.

This repository is provided for portfolio and educational reference.
Commercial use, redistribution, or republishing without prior permission is not permitted.
