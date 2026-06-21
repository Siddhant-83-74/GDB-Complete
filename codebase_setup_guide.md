# [Project Name] - Codebase Setup Guide

Welcome to the [Project Name] codebase! This document provides a comprehensive guide on understanding the project structure, architecture, and instructions on how to import, build, and run the application.

---

## 1. Directory Structure

The repository is organized into a microservices-based backend and a modern React frontend.

```text
[Project Name]/
├── frontend/                     # React Frontend Application
│   ├── public/                   # Static assets (favicon, images)
│   ├── src/                      # React source code
│   │   ├── components/           # Reusable UI components
│   │   ├── pages/                # Page-level components
│   │   ├── services/             # API integration logic
│   │   ├── store/                # Global state management (Zustand/Redux)
│   │   ├── App.jsx               # Root application component
│   │   └── main.jsx              # Entry point
│   ├── package.json              # Node.js dependencies & scripts
│   └── vite.config.js            # Vite bundler configuration
│
├── backend-services/             # Spring Boot Microservices
│   ├── auth-service/             # Authentication & JWT management
│   ├── users-service/            # User profile management
│   ├── account-service/          # Account & balance management
│   ├── transactions-service/     # Deposits, withdrawals, and transfers
│   └── ...                       # Additional microservices
│       ├── src/main/java/        # Java source code
│       ├── src/main/resources/   # Application properties & DB migrations
│       └── pom.xml               # Maven configuration
└── README.md                     # Project overview
```

---

## 2. System Architecture & Data Flow

[Project Name] follows a modern N-Tier microservices architecture. The data flows sequentially through the following layers:

1. **React Frontend (Presentation Layer):**
   Captures user input and sends HTTP requests (via Axios/Fetch) to the backend.
2. **Spring Boot Controller (API Layer):**
   Receives the HTTP requests, validates the input payload, and routes it to the appropriate service.
3. **Service Layer (Business Logic):**
   Contains the core business rules. It performs computations, orchestrates multiple operations, and applies security constraints.
4. **Repository Layer (JPA/Hibernate):**
   Acts as the Data Access Object (DAO). It translates Java operations into SQL queries.
5. **PostgreSQL Database (Data Layer):**
   Persists the data securely on the disk.

> **Data Flow Diagram:**
> `React Client` ➔ `REST API` ➔ `Controller` ➔ `Service` ➔ `Repository` ➔ `PostgreSQL`

---

## 3. Importing the Project into your IDE

> [!NOTE]
> The project codebase is distributed as a ZIP archive. Extract the ZIP file to your local workspace before importing.
> 
> * **Preferred/Recommended IDE:** Opening the backend in **IntelliJ IDEA** is highly recommended for the best experience.
> * **Second Option:** You can also use **Eclipse** or **VS Code** (configured with appropriate Java support) as a secondary option.

### Backend (IntelliJ IDEA)
1. Open IntelliJ IDEA and select **File ➔ Open**.
2. Navigate to the specific microservice folder (e.g., `auth-service`) containing the `pom.xml` file.
3. Click **Open** as a Project.
4. IntelliJ will automatically detect the Maven configuration. Wait for the dependencies to download and index.
5. Repeat for any other microservices you need to run.

### Frontend (VS Code)
1. Open Visual Studio Code.
2. Select **File ➔ Open Folder** (or use `Ctrl+K Ctrl+O`).
3. Select the `frontend/` directory.
4. Open the integrated terminal (`Ctrl+\``) and ensure you are in the frontend root directory.

---

## 4. Build, Compile, and Run Instructions

To run the complete application, you must start the backend microservices and the React frontend. Below is a reference table showing the directory path, default port, and start command for each service.

### Services Reference Table

| Service Name | Directory Path (from repository root) | Port | Run Command |
| :--- | :--- | :--- | :--- |
| **Account Service** | `account-service/` | `8001` | `mvn spring-boot:run` |
| **Transactions Service** | `transactions-service/` | `8002` | `mvn spring-boot:run` |
| **Users Service** | `users-service/` | `8003` | `mvn spring-boot:run` |
| **Auth Service** | `auth-service/` | `8004` | `mvn spring-boot:run` |
| **Aadhar Service** | `aadhar-service/` | `8005` | `mvn spring-boot:run` |
| **Company Service** | `company-service/` | `8006` | `mvn spring-boot:run` |
| **Payment Gateway Service** | `payment-gateway-service/` | `8008` | `mvn spring-boot:run` |
| **React Frontend** | `frontend/` | `3000` | `npm run dev` |

---

### Step-by-Step Backend Run Instructions

*Ensure your PostgreSQL database server is running and the 4 target databases (`gdb_users_db`, `gdb_auth_db`, `gdb_accounts_db`, `gdb_transactions_db`) are created before starting the backend.*

For **each** microservice (open a new terminal/command prompt window for each):
1. Navigate to the microservice folder:
   ```bash
   cd <directory-path>
   # Example: cd auth-service
   ```
2. Build, compile, and run the microservice:
   ```bash
   # Build/compile (skip tests in development to save time)
   mvn clean install -DskipTests

   # Run the application
   mvn spring-boot:run
   ```

---

### Step-by-Step Frontend Run Instructions

1. Open a new terminal window and navigate to the `frontend/` directory:
   ```bash
   cd frontend
   ```
2. Set up the environment configuration by copying `.env.example` to `.env`:
   ```bash
   # On Windows (PowerShell / Command Prompt)
   copy .env.example .env

   # On macOS / Linux
   cp .env.example .env
   ```
3. Install the required Node.js dependencies:
   ```bash
   npm install
   ```
4. Start the development server:
   ```bash
   npm run dev
   ```
5. Access the application in your web browser at: `http://localhost:3000`

---

## 5. Default Test Credentials

Once the backend and frontend are running, you can log in to the application using any of the following pre-seeded user roles:

| Role | Login ID | Password |
| :--- | :--- | :--- |
| **Admin** | `admin` | `password` |
| **Manager** | `manager` | `password` |
| **Teller** | `teller` | `password` |
