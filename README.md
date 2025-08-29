# Personnel Management System

A microservices-based personnel management system built with Spring Boot and React for managing employees, departments, tasks, and meetings.

## System Overview

The system consists of a few microservices and a React frontend:

- **Eureka Server** - Service discovery (port 8761)
- **Auth Service** - User authentication and management (port 8082)
- **Personnel Service** - Core personnel data (port 8081)
- **Notification Service** - Email notifications (port 8083)
- **Frontend** - React web interface (port 3000)

## Login Information

### Default Users

- **Admin**: username: `admin`, password: `admin`
- **Pre-created users**: password: `123456`
- **New users**: Passwords are automatically sent to their email addresses

## How to Run

### 1. Start RabbitMQ

Ensure RabbitMQ is running on your system.

### 2. Start Services

```bash
# Eureka Server
cd eureka-server
mvn spring-boot:run

# Auth Service
cd auth-service
mvn spring-boot:run

# Personnel Service
cd personnel-service
mvn spring-boot:run

# Notification Service
cd notification-service
mvn spring-boot:run
```

### 3. Start Frontend

```bash
cd frontend
npm start
```

## Services

### Eureka Server

- Service discovery and registration
- Available at: http://localhost:8761

### Auth Service

- JWT-based authentication
- User roles: ADMIN, HR, HEAD, EMPLOYEE
- Available at: http://localhost:8082
- API docs: http://localhost:8082/swagger-ui.html

### Personnel Service

- Employee and department management
- Task and meeting management
- Available at: http://localhost:8081
- API docs: http://localhost:8081/swagger-ui.html

### Notification Service

- Email notifications for events
- Gmail SMTP configured for development/testing (test gmail credentials in notification-service/../application.yml)
- Available at: http://localhost:8083
- API docs: http://localhost:8083/swagger-ui.html

### Frontend

- React application with TypeScript
- Role-based dashboards
- Available at: http://localhost:3000
