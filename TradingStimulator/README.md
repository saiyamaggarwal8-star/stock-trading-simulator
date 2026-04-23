# Trading Stimulator

A full-stack trading simulation application with a Spring Boot backend and a React/Vite frontend.

## Prerequisites

- **Java**: JDK 17 or higher
- **Node.js**: v16 or higher
- **MySQL**: Running on port 3306 with a database named `trading_simulator`
- **Maven**: For building the backend

## Getting Started

### 1. Database Configuration

Ensure your MySQL server is running and configured in `src/main/resources/application.properties`.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/trading_simulator
spring.datasource.username=root
spring.datasource.password=your_password
```

### 2. Running the Backend

Navigate to the root directory and run:

```bash
mvn spring-boot:run
```

The backend API will be available at `http://localhost:8081`.

### 3. Running the Frontend

Navigate to the `frontend` directory and run:

```bash
cd frontend
npm install
npm run dev
```

The frontend application will be available at `http://localhost:33334`.

> [!NOTE]
> We use `localhost:8081` for the backend and `localhost:33334` for the frontend to ensure compatibility with Google OAuth2 redirects.

## Build and Preview

### Backend

To build the JAR file:

```bash
mvn clean package
```

### Frontend

To build the static assets and preview the production build:

```bash
cd frontend
npm run build
npm run preview
```

## Features

- Real-time stock price simulation
- Portfolio management
- Trade history tracking
- Interactive charts
