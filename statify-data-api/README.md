# 🎧 Statify-data-api

**Statify-data-api** is a Kotlin microservice built with **Spring Boot** and **Netflix DGS GraphQL**.  
It provides a **GraphQL API** to access users’ Spotify library and analytics data.

---

## 🔧 Technologies Used

- Kotlin
- Spring Boot
- Netflix DGS GraphQL
- PostgreSQL (Spring Data JPA)
- Docker & Docker Compose

---

## 🚀 Getting Started

### ▶️ Run with Docker Compose

```bash
git clone https://github.com/danilabubnov/Statify.git
cd statify-data-api
cp .env.example .env   # Fill in your environment variables
docker-compose up --build
```

## 📡 GraphQL API

**🔗 Endpoint:**  
[`http://localhost:8081/graphql`](http://localhost:8081/graphql)

**🧪 Explore & Test:**  
Open the [Apollo Studio Sandbox Explorer](https://studio.apollographql.com/sandbox/explorer)  
and paste the above URL into the **GraphQL endpoint** field.

