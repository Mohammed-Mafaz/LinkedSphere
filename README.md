# 🌐 LinkedSphere

LinkedSphere is a **distributed social networking platform** inspired by LinkedIn, built using **Spring Boot Microservices**, **Spring Cloud**, **Kafka**, and **Docker**.  
It demonstrates advanced concepts like **API Gateway**, **Service Discovery**, **Event-Driven Communication**, and **Inter-service Authentication** using JWT.

---

## 🚀 Features

- ✅ **Microservices Architecture** with independent services
- 🔐 **JWT Authentication** and API Gateway filtering
- 📡 **Service Discovery** using Eureka
- 🌉 **Spring Cloud Gateway** for centralized routing and logging
- 📨 **Event-driven notifications** using Kafka
- 🧠 **Graph-based connections** using Neo4j for first-degree connection queries
- 🗄 **PostgreSQL** for relational data (Users, Posts, Notifications)
- 🐳 **Docker Compose** for containerized deployment
- ✨ Clean separation between Auth, Posts, Connections, Notifications & Gateway

---

## 🧭 Architecture Overview

```
                        ┌──────────────────────────┐
                        │      API Gateway         │  ← JWT Auth, Logging Filters
                        └─────────────┬────────────┘
                                      │
       ┌──────────────────────────────┼──────────────────────────────┐
       │                              │                              │
┌─────────────┐              ┌────────────────┐              ┌─────────────────────┐
│ UserService │◄────────────►│ Posts Service  │◄────────────►│ Connections Service │
└─────────────┘              └────────────────┘              └─────────────────────┘
       │                             │                             │
       ▼                             ▼                             ▼
  PostgreSQL DB                PostgreSQL DB                   Neo4j DB (Graph)

       └──────────────────────────────┬──────────────────────────────┘
                                      │
                                      ▼
                          ┌────────────────────────┐
                          │ Notification Service   │  ← Kafka Consumers
                          └─────────────┬──────────┘
                                        │
                                        ▼
                                   PostgreSQL DB

                          ┌────────────────────────┐
                          │    Discovery Server    │
                          │         (Eureka)       │
                          └────────────────────────┘

```

---

## 🧩 Microservices Overview

| Service              | Port | Description                                                                 |
|-----------------------|------|-----------------------------------------------------------------------------|
| **API Gateway**       | 8080 | Central entry point; JWT auth filter & logging                              |
| **Discovery Server**  | 8761 | Eureka Service Registry for service discovery                              |
| **User Service**      | —    | Handles signup, login, and authentication                                  |
| **Posts Service**     | —    | CRUD for posts, likes, retrieving posts by user                            |
| **Connections Service** | —  | Manages connection requests, accept/reject, and first-degree connections   |
| **Notification Service** | — | Kafka consumer that listens to events and stores notifications             |
| **Kafka & UI**        | 9092 / 8090 | Event streaming backbone & UI dashboard                                  |
| **Databases**         | Various | PostgreSQL for users/posts/notifications, Neo4j for connections graph |

---

## ⚙️ Tech Stack

- **Backend:** Spring Boot, Spring Cloud, Spring Security
- **Event Bus:** Apache Kafka
- **Service Discovery:** Netflix Eureka
- **Gateway:** Spring Cloud Gateway
- **Databases:** PostgreSQL, Neo4j
- **Auth:** JWT
- **Inter-Service Communication:** REST + Feign Clients
- **Containerization:** Docker & Docker Compose

---

## 🛠️ Setup & Installation

### Prerequisites
- [Docker](https://www.docker.com/)
- [JDK 21+](https://adoptium.net/)
- [Maven](https://maven.apache.org/) (if building locally)

### 🐳 Run with Docker Compose

```bash
# From the root of the project
docker-compose up -d
```

This will spin up:
- Kafka & Kafka UI
- PostgreSQL for multiple services
- Neo4j for Connections
- All Microservices (User, Posts, Connections, Notification, Gateway, Discovery)

Check Eureka Dashboard at 👉 [http://localhost:8761](http://localhost:8761)  
Access API Gateway at 👉 [http://localhost:8080](http://localhost:8080)

---

## 🔑 Authentication

- Users sign up & log in via `/auth/signup` and `/auth/login`.
- JWT is returned upon login.
- All subsequent requests to microservices go through **API Gateway** with `Authorization: Bearer <token>` header.
- The **AuthenticationFilter** in Gateway extracts the token, validates it, and appends `X-User-Id` header for downstream services.

---

## 📡 Event Flow Example (Post Creation)

1. **User** creates a post via `POST /core` on Posts Service.
2. Posts Service emits a `post-created` event to Kafka.
3. Notification Service listens to this event and retrieves **first-degree connections** using `ConnectionsFeignClient`.
4. Notifications are generated and stored for all those connections.

---

## 🧪 Example API Endpoints

### Auth
```http
POST /auth/signup
POST /auth/login
```

### Posts
```http
POST /core                    # Create post
GET /core/{postId}            # Get single post
GET /core/users/{userId}/allPosts  # Get all posts of user
POST /likes/{postId}          # Like post
DELETE /likes/{postId}        # Unlike post
```

### Connections
```http
GET /core/first-degree
POST /core/request/{userId}
POST /core/accept/{userId}
POST /core/reject/{userId}
```

---

## 🧠 Key Components

### 🔸 API Gateway Filters
- **GlobalLoggingFilter** — Logs every incoming and outgoing request.
- **AuthenticationFilter** — Validates JWT and propagates `X-User-Id`.

### 🔸 User Interceptor
Injects user ID into `ThreadLocal` context for downstream processing (in Posts / Connections).

### 🔸 Kafka Topics
- `post-created`, `post-liked`
- `send-connection-request-topic`, `accept-connection-request-topic`

---

## 📸 Monitoring

- Kafka UI → [http://localhost:8090](http://localhost:8090)
- Eureka Dashboard → [http://localhost:8761](http://localhost:8761)

---

## 🤝 Contribution

1. Fork the repo
2. Create a new feature branch (`feature/your-feature`)
3. Commit and push your changes
4. Open a Pull Request 🚀

---

## ✨ Acknowledgments

Special thanks to **Spring Cloud**, **Kafka**, and **Neo4j** communities for their excellent tooling and documentation.
