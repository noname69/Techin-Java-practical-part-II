# Task Manager 05 JPA Persistence

## Overview
Before you begin this task, copy your full working code from Task 04 into this project.

This student starter does not include the previous task implementation on purpose. First bring over your own:
- controller
- dto
- exception
- model
- repository
- service

After that, migrate the persistence layer from an in-memory repository to a real database using Spring Data JPA and H2.

This task is intentionally narrow:
- keep the Task 04 API unchanged
- keep DTOs, validation, mapper, and exception handling
- change only how data is stored

The main lesson is persistence migration, not API redesign.

## What You Learn
- how `@Entity` maps a Java class to a database table
- how `@Id` and `@GeneratedValue` work
- how Spring Data JPA replaces manual repository implementations
- how H2 can be used as a simple in-memory database
- how to keep controller and service code stable while changing persistence underneath

## What Must Stay The Same
Keep from Task 04:
- `/api/tasks` endpoints
- request and response DTOs
- `TaskMapper`
- validation rules
- `GlobalExceptionHandler`
- `400`, `404`, and `409` behavior

Do not add:
- relationships
- custom JPQL
- custom SQL
- Flyway or Liquibase
- new business rules

## What You Must Change
Replace the in-memory repository approach with Spring Data JPA.

That means:
- `Task` becomes a JPA entity
- repository becomes a Spring Data repository interface
- data is stored in H2 instead of a `List`

## Step 1: Copy Your Task 04 Code
Begin by copying your completed Task 04 code into this project:
- DTOs already exist
- validation already works
- global exception handling already works
- `Task` already contains:
  - `id`
  - `title`
  - `description`
  - `done`
  - `dueDate`

## Step 2: Add Persistence Dependencies
Your project now needs:
- `spring-boot-starter-data-jpa`
- `com.h2database:h2`

Keep the existing web and validation dependencies too.

## Step 3: Configure H2
Add simple datasource and JPA settings in `application.properties`.

Recommended settings:
```properties
spring.datasource.url=jdbc:h2:mem:taskdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

## Step 4: Turn `Task` Into An Entity
Update `Task` so it becomes a JPA entity.

At minimum:
- add `@Entity`
- mark the id with `@Id`
- add `@GeneratedValue`

Keep `Task` as the internal model. Do not replace it with DTOs in service or repository layers.

## Step 5: Replace The Repository
Remove the in-memory implementation approach.

Your repository should become:
- `TaskRepository extends JpaRepository<Task, Long>`

Keep a derived query method for search:
- `List<Task> findByDone(boolean done)`

You should no longer need:
- manual list storage
- manual id generation
- `InMemoryTaskRepository`

## Step 6: Keep Service Behavior
Your service should still behave like Task 04:
- create forces `done = false`
- update changes editable fields
- repeated `done=true` patch returns `409`
- missing ids return `404`

The service should still call the repository, but now the repository is JPA-backed.

## Step 7: Keep The API Stable
Your API must still support:
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `GET /api/tasks/search?done=true`
- `POST /api/tasks`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}/done?value=true`
- `DELETE /api/tasks/{id}`

The request and response JSON should stay compatible with Task 04.

## Rules
- keep validation in DTOs
- keep global exception handling
- keep `Task` as the entity
- use H2
- use Spring Data JPA
- do not add relationships
- do not add custom queries unless the tests require them
- do not change the public tests

## How To Work
1. Start from the copied Task 04 solution code.
2. Add JPA and H2 dependencies.
3. Configure H2 in `application.properties`.
4. Annotate `Task` with JPA mapping annotations.
5. Change `TaskRepository` to extend `JpaRepository<Task, Long>`.
6. Remove the in-memory repository implementation.
7. Keep the service and controller behavior unchanged.
8. Run tests and fix failing cases.

## Run Locally
```bash
mvn test
```

## Acceptance Checks
- the application starts with H2
- `Task` is a JPA entity
- `TaskRepository` is a Spring Data JPA repository
- create persists a task and returns a generated id
- search by `done` still works
- validation and exception handling from Task 04 still work unchanged
