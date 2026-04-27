# Task Manager Security JWT Authentication and Authorization

## Overview
Before you begin this task, copy your full working code from Task 08 into this project.

This starter intentionally does not include your previous implementation. First bring over your own:
- controllers
- DTOs and mappers
- exceptions and global error handling
- models
- repositories
- services
- JPA and H2 configuration

After that, add JWT-based authentication and simple role-based authorization.

You can study the local `security-jwt-demo` project as an example. You do not need to copy its package names or exact class structure. The tests check API behavior, not implementation details.

## Main Goal
Add authentication and authorization to the Task Manager API.

Authentication means:
- who are you?

Authorization means:
- what are you allowed to do?

This task uses stateless JWT authentication:
- user logs in with email and password
- API returns a JWT access token
- client sends later requests with `Authorization: Bearer <token>`

## What Must Stay From Task 08
Keep:
- projects
- tasks
- users
- task assignment
- validation
- global error handling
- JPA with H2
- existing Task 08 business rules

Do not redesign the whole Task Manager API. Add security around it.

## Required New API
Add public auth endpoints:
- `POST /api/auth/register`
- `POST /api/auth/login`

Recommended optional endpoint:
- `GET /api/auth/me`

### Register Request
```json
{
  "name": "Alice Student",
  "email": "alice@example.com",
  "password": "password123"
}
```

### Register Response
```json
{
  "id": 1,
  "name": "Alice Student",
  "email": "alice@example.com",
  "roles": ["USER"]
}
```

Do not expose password or password hash in any response.

### Login Request
```json
{
  "email": "alice@example.com",
  "password": "password123"
}
```

### Login Response
```json
{
  "tokenType": "Bearer",
  "accessToken": "...",
  "email": "alice@example.com",
  "roles": ["USER"]
}
```

## Authorization Rules
Use a small role model:
- `USER`
- `ADMIN`

Required behavior:
- anonymous users can only register and log in
- project and task endpoints require a valid token
- normal authenticated users can read and create projects/tasks
- normal authenticated users can update task status
- `/api/users` is admin-only
- normal users get `403 Forbidden` for `/api/users`
- missing or invalid token returns `401 Unauthorized`

You need one admin account for tests/manual checks:
- email: `admin@example.com`
- password: `admin123`
- role: `ADMIN`

## Implementation Hints
1. Copy your full Task 08 code into this project.
2. Add Spring Security dependencies.
3. Extend `User` so it can store a hashed password and roles.
4. Hash passwords with `PasswordEncoder`.
5. Add register and login endpoints.
6. Issue a JWT after successful login.
7. Configure stateless bearer-token authentication.
8. Protect project, task, and user endpoints.
9. Make `/api/users` admin-only.
10. Keep response DTOs clean: no password fields.

Useful ideas from `security-jwt-demo`:
- `SecurityFilterChain`
- `PasswordEncoder`
- JWT encoder/decoder
- `Authorization: Bearer <token>`
- roles converted to Spring authorities

## Testing Direction
Tests are intentionally black-box API tests.

They do not require:
- exact class names
- exact method names
- exact package structure
- exact JWT service implementation
- exact security configuration class name

They do require the API behavior described above.

## Run Locally
```bash
mvn test
```
