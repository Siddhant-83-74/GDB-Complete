# MOD10-CR-01: Create API Gateway Microservice

## Objective
Implement a centralized Spring Cloud API Gateway service running on port `8000` to route incoming traffic to respective microservices.

## Instructions
1. Initialize a new Spring Boot application named `gateway-service` in this folder.
2. Add the `spring-cloud-starter-gateway` dependency.
3. Configure `application.yml` to set the port to `8000`.
4. Define routing predicates so that:
   - `/api/v1/auth/**` routes to `http://localhost:8004` (auth-service)
   - `/api/v1/users/**` routes to `http://localhost:8003` (users-service)
   - `/api/v1/accounts/**` routes to `http://localhost:8001` (account-service)
   - `/api/v1/transactions/**` routes to `http://localhost:8002` (transactions-service)
5. Test the gateway by changing your frontend environment variables to point to `http://localhost:8000` instead of multiple separate ports.
