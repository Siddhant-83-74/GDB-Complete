# MOD12-CR-01: Implement Service Discovery with Eureka Server

## Objective
Establish a centralized Eureka Service Discovery Registry Server running on port `8761`.

## Instructions
1. Initialize a new Spring Boot application named `eureka-server` in this folder.
2. Add the `spring-cloud-starter-netflix-eureka-server` dependency.
3. Annotate the main boot application class with `@EnableEurekaServer`.
4. Configure `application.yml` to:
   - Run on port `8761`.
   - Prevent registration of itself:
     ```yaml
     eureka:
       client:
         register-with-eureka: false
         fetch-registry: false
     ```
5. Add Eureka client dependencies (`spring-cloud-starter-netflix-eureka-client`) to all microservices (`auth-service`, `users-service`, `account-service`, and `transactions-service`).
6. Configure the microservices' `application.yml` files to discover and register with the Eureka server url: `http://localhost:8761/eureka/`.
7. Once configured, load the dashboard page at `http://localhost:8761` and verify that all registered instances are displayed correctly.
