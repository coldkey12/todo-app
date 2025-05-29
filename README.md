# 📝 Todo App — Spring Boot + JWT + PostgreSQL

A secure and scalable Todo application built with Spring Boot, JWT-based authentication, and PostgreSQL.

**🌐 Deployed App**: [Google cloud run app](https://todo-app-413057889128.europe-west1.run.app)

**Swagger Documentation** : https://todo-app-413057889128.europe-west1.run.app/swagger-ui.html

**Video of demo**: https://www.loom.com/share/70809eea3e3144ef999ba8a4c5a13ccf?sid=d3413a00-66d3-4165-8a96-e9a7d6a88777



---

## 🚀 Features

- ✅ User registration and login with JWT authentication  
- 🔐 Role-based access control (Admin/User)  
- 📋 Create, update, delete, and list personal tasks  
- 🔄 Access token refresh and logout endpoints  
- 📚 Interactive API documentation via Swagger UI  

---

## 🛠 Tech Stack

- **Language**: Java 17+  
- **Framework**: Spring Boot  
- **Security**: Spring Security + JWT  
- **Database**: PostgreSQL  
- **ORM**: Spring Data JPA (Hibernate)  
- **Utilities**: Lombok  
- **API Docs**: Swagger / OpenAPI  

---

## ⚙️ Getting Started

### 📦 Prerequisites

Ensure you have the following installed:

- Java 17 or higher  
- Maven  
- PostgreSQL  

---

### 🧾 Configuration

Update the `src/main/resources/application.yml` file with your database and JWT settings:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/<your_db>
    username: <your_user>
    password: <your_password>

jwt:
  secret: <your_jwt_secret>
  expiration: 3600000
```

---

### 🏗 Build & Run

To build and run the application locally:

```bash
mvn clean install
mvn spring-boot:run
```

Visit: [http://localhost:8080](http://localhost:8080)

---

### 📘 API Documentation

Access Swagger UI at:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 📂 Project Structure

```text
src/main/java/kz/don/todo_app/
├── application/          # Application services and use cases
│   └── service/
├── config/              # Configuration classes
├── domain/             # Core domain model
│   ├── entity/         # JPA entities
│   ├── repository/     # Repository interfaces
├── infrastructure/     # External implementations
│   ├── persistence/    # Repository implementations
│   └── security/       # Security related classes
│       ├── jwt/
│       └── config/
└── web/               # Web layer
    ├── controller/    # REST controllers
    ├── dto/          # Data Transfer Objects
    │   ├── request/
    │   └── response/
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
