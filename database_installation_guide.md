# [Project Name] - Database Installation Guide

This document provides a step-by-step guide to installing PostgreSQL, configuring the pgAdmin 4 management tool, and setting up the database schema for the [Project Name] application.

---

## 1. Download and Install PostgreSQL

1. **Download the Installer:**
   Navigate to the official EnterpriseDB download page:
   [PostgreSQL Official Downloads](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)
2. **Run the Installer:**
   - Select the version compatible with your OS (PostgreSQL 14, 15, or 16 are recommended).
   - During installation, keep the default port **`5432`**.
   - **Important:** You will be prompted to set a password for the `postgres` superuser. Please set it to something memorable (e.g., `postgres` or `admin123`) as you will need it later.
   - Proceed through the prompts and complete the installation.

---

## 2. Registering the Server in pgAdmin 4

pgAdmin 4 is the graphical user interface used to manage PostgreSQL databases. It is typically bundled with the EDB installer.

1. **Open pgAdmin 4:** Search your computer for "pgAdmin 4" and open it. You may be prompted to set a master password for the application itself.
2. **Register a New Server:**
   - In the left sidebar, right-click on **Servers** ➔ **Register** ➔ **Server...**
   - **General Tab:** Enter a name for the connection (e.g., `[Project Name] Local DB`).
   - **Connection Tab:**
     - **Host name/address:** `localhost`
     - **Port:** `5432`
     - **Maintenance database:** `postgres`
     - **Username:** `postgres`
     - **Password:** *(Enter the superuser password you set during installation)*
   - Click **Save**.

---

## 3. Creating the Target Database

1. Expand your newly registered server in the left sidebar.
2. Right-click on **Databases** ➔ **Create** ➔ **Database...**
3. Enter the database name required by the project (e.g., `[Project Name]_db` or `gdb_users_db`).
4. Click **Save**.

---

## 4. Running the Initial Schema Script

We need to create the tables and insert the initial seed data. 

1. Right-click on your newly created database and select **Query Tool**.
2. Copy and paste the following `schema.sql` template into the query editor:

```sql
-- ==========================================
-- [Project Name] - Initial Database Schema
-- ==========================================

-- 1. Create Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create Accounts Table (Foreign Key Example)
CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Insert Initial Seed Data (Default Users with password: password)
INSERT INTO users (username, login_id, password, role, is_active)
VALUES 
    ('Admin', 'admin', '$2a$10$kDHAS/OeBuqJT7pV03Yc1.9MO7mD9u9eS6xjRa6VIO9.THN5XWKWq', 'ADMIN', TRUE),
    ('Manager', 'manager', '$2a$10$kDHAS/OeBuqJT7pV03Yc1.9MO7mD9u9eS6xjRa6VIO9.THN5XWKWq', 'MANAGER', TRUE),
    ('Teller', 'teller', '$2a$10$kDHAS/OeBuqJT7pV03Yc1.9MO7mD9u9eS6xjRa6VIO9.THN5XWKWq', 'TELLER', TRUE)
ON CONFLICT (login_id) DO NOTHING;
```

3. Click the **Execute/Refresh (▶)** button in the toolbar (or press `F5`). The message pane should indicate "Query returned successfully".

---

## 5. Application Configuration

To connect the application to your new database, you must update the configuration files.

### Backend (Spring Boot)
Open the `src/main/resources/application.yml` (or `application.properties`) file in your microservice and update the datasource section:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/[Project Name]_db
    username: postgres
    password: [Your_Postgres_Password]
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate # or 'update' for dev environments
    show-sql: true
```

### Frontend (React)
While the frontend does not connect to the database directly, it must point to the backend API. Update the `.env` file in the frontend root directory:

```env
# Frontend Environment Configuration
VITE_API_BASE_URL=http://localhost:8004
# (Adjust the port to match your backend Gateway or Service)
```
