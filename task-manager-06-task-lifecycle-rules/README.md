# Task Manager 06 Task Lifecycle Rules

## Overview
Before you begin this task, copy your full working code from Task 05 into this project.

This student starter does not include the previous task implementation on purpose. First bring over your own:
- controller
- dto
- exception
- model
- repository
- service
- JPA and H2 configuration

After that, enrich the domain with lifecycle rules.

This task is not about CRUD anymore. CRUD is already done.

The new lesson is:
- model richer task state
- enforce transition rules in the service layer
- persist that richer state correctly

## Main Goal
Replace the simple `done` flag with a status-based lifecycle.

Use:
- `TaskStatus`
- `completedAt`

## What You Learn
- how domain rules differ from validation rules
- how to model task state with an enum
- how service methods enforce allowed transitions
- why invalid transitions should return `409 Conflict`
- how richer state is stored with JPA

## What Must Stay The Same
Keep from Task 05:
- JPA persistence
- H2 configuration
- layered structure
- DTO boundary
- mapper
- validation for title, description, and dueDate
- global exception handling

## New Domain Model
Replace:
- `done: boolean`

With:
- `status: TaskStatus`
- `completedAt`

Recommended enum values:
- `TODO`
- `DONE`

Recommended `completedAt` type:
- `LocalDateTime`

## API Direction
Keep the existing API structure as similar as possible.

Recommended endpoints:
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `GET /api/tasks/search?status=TODO`
- `POST /api/tasks`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}/status?value=DONE`
- `DELETE /api/tasks/{id}`

## Required Business Rules
- new tasks always start with status `TODO`
- new tasks start with `completedAt = null`
- changing a task from `TODO` to `DONE` sets `completedAt`
- trying to complete an already completed task returns `409 Conflict`
- moving from `DONE` back to `TODO` is not allowed in this task and returns `409 Conflict`

## DTO Direction
Recommended request/response shape:

### `CreateTaskRequest`
- `title`
- `description`
- `dueDate`

### `UpdateTaskRequest`
- `title`
- `description`
- `status`
- `dueDate`

### `TaskResponse`
- `id`
- `title`
- `description`
- `status`
- `dueDate`
- `completedAt`

## Suggested Step-By-Step Work
1. Start from the copied Task 05 solution code.
2. Create `TaskStatus`.
3. Replace `done` with `status` in the entity, DTOs, mapper, repository, and service.
4. Add `completedAt`.
5. Update repository queries from `findByDone(...)` to `findByStatus(...)`.
6. Update controller endpoints that search or patch state.
7. Enforce lifecycle rules in the service layer.
8. Keep `409 Conflict` for illegal transitions.
9. Run tests and fix failing cases.

## Rules
- do not remove JPA
- do not add relationships
- do not redesign validation handling
- do not move business rules into the controller
- do not change the public tests

## Run Locally
```bash
mvn test
```
