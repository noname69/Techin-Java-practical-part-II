# Task Manager 08 Users and Task Assignment

## Overview
Before you begin this task, copy your full working code from Task 07 into this project.

This starter intentionally does not include your previous implementation. First bring over your own:
- controller
- dto
- exception
- model
- repository
- service
- JPA and H2 configuration

After that, extend the application with a new `User` entity and optional task assignment.

The new focus in this task is:
- a second real relationship
- using several repositories in one service
- assignment rules across entities
- keeping entity relationships out of JSON by using DTOs

## What Must Stay The Same
Keep from Task 07:
- JPA with H2
- layered structure
- DTO boundary
- validation
- global exception handling
- project-scoped task creation:
  - `POST /api/projects/{projectId}/tasks`
- task-by-id operations:
  - `GET /api/tasks/{id}`
  - `PUT /api/tasks/{id}`
  - `PATCH /api/tasks/{id}/status?value=DONE`
  - `DELETE /api/tasks/{id}`
- existing project rules:
  - archived project cannot accept new tasks
  - project name must be unique
- existing task lifecycle rules:
  - new task starts in `TODO`
  - repeated `DONE` update returns `409`
  - `DONE -> TODO` returns `409`

## New Domain Model
Add a new `User` entity:
- `id`
- `name`
- `email`

Update `Task` so it may be assigned to one user:
- add field `assignee`
- use `@ManyToOne`

Keep assignment optional:
- a task may have no assignee
- `assigneeId` may be `null`

Do not return entities directly from controllers.

## API Direction
### User endpoints
Add:
- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

Required status codes:
- `200 OK` for successful reads and updates
- `201 Created` for create
- `204 No Content` for delete
- `404 Not Found` for missing ids
- `409 Conflict` for duplicate email

These are application users only. Do not add authentication yet.

### Task endpoints
Keep the Task 07 shape:
- `POST /api/projects/{projectId}/tasks`
- `GET /api/projects/{id}/tasks`
- `GET /api/projects/{id}/tasks?status=TODO`
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}/status?value=DONE`
- `DELETE /api/tasks/{id}`

## DTO Direction
Use records.

### New user DTOs
Create:
- `CreateUserRequest`
  - `name`
  - `email`
- `UpdateUserRequest`
  - `name`
  - `email`
- `UserResponse`
  - `id`
  - `name`
  - `email`
- `UserSummaryResponse`
  - `id`
  - `name`
  - `email`

### Task DTO changes
`CreateTaskRequest`:
- keep task fields from Task 07
- add optional `assigneeId`

`UpdateTaskRequest`:
- keep task fields from Task 07
- keep `projectId`
- add optional `assigneeId`

`TaskResponse`:
- keep existing task fields
- keep nested `project`
- add nested `assignee`

Use summary DTOs in responses:
- `ProjectSummaryResponse project`
- `UserSummaryResponse assignee`

## Validation Direction
Keep existing task validation.

Add validation to user DTOs:

`name`
- `@NotBlank`
- `@Size(min = 3, max = 100)`

`email`
- `@NotBlank`
- `@Email`

## Business Rules
- task can be assigned only to an existing user
- completed task cannot be reassigned
- user email must be unique

In practice this means:
- create task with missing `assigneeId` -> `404`
- update task with missing `assigneeId` -> `404`
- create or update user with duplicate email -> `409`
- changing assignee on a completed task -> `409`

If `assigneeId` is `null`, keep the task unassigned.

## Implementation Hints
- keep entities internal, do not return them directly
- add `UserRepository extends JpaRepository<User, Long>`
- add:
  - `boolean existsByEmail(String email)`
  - `boolean existsByEmailAndIdNot(String email, Long id)`
- add `UserService` and `DefaultUserService`
- check user existence in the service layer, not in the controller
- update task mapping so `TaskResponse` includes nested assignee summary
- reuse the same error JSON style from earlier tasks
- reuse `IllegalTaskStateException` when a completed task is reassigned

## JSON Examples
Create user request:
```json
{
  "name": "Alice Teacher",
  "email": "alice@example.com"
}
```

Create user response:
```json
{
  "id": 1,
  "name": "Alice Teacher",
  "email": "alice@example.com"
}
```

Create assigned task:
```http
POST /api/projects/1/tasks
```

```json
{
  "title": "Prepare lesson",
  "description": "Add user assignment support",
  "dueDate": "2099-05-01",
  "assigneeId": 1
}
```

Assigned task response:
```json
{
  "id": 1,
  "title": "Prepare lesson",
  "description": "Add user assignment support",
  "status": "TODO",
  "dueDate": "2099-05-01",
  "completedAt": null,
  "project": {
    "id": 1,
    "name": "Spring Course",
    "archived": false
  },
  "assignee": {
    "id": 1,
    "name": "Alice Teacher",
    "email": "alice@example.com"
  }
}
```

Create unassigned task:
```json
{
  "title": "Unassigned task",
  "description": "No user yet",
  "dueDate": "2099-05-02",
  "assigneeId": null
}
```

Duplicate email error:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "User with email 'alice@example.com' already exists."
}
```

Missing assignee error:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User with id 99 was not found."
}
```

Completed task reassignment error:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Task with id 1 is completed and cannot be reassigned."
}
```

## Suggested Step-By-Step Work
1. Copy your full Task 07 code into this project.
2. Create `User` entity and `UserRepository`.
3. Add user DTOs and a `UserMapper`.
4. Add `UserService` and `UserController`.
5. Update `Task` so it can reference an optional assignee.
6. Update task DTOs with `assigneeId`.
7. Update mappers so task responses include nested assignee summary.
8. Refactor `TaskService` so creation and update resolve `assigneeId`.
9. Add `UserNotFoundException` and `DuplicateUserEmailException`.
10. Update `GlobalExceptionHandler`.
11. Run tests and fix the remaining failing cases.

## Rules
- do not return entities directly from controllers
- do not expose task collections from `UserResponse`
- do not move existence checks into controllers
- do not remove JPA or H2
- do not remove project rules or task lifecycle rules
- do not add authentication or Spring Security in this task
- do not add `TaskPriority` in this task
- do not change the public tests

## Run Locally
```bash
mvn test
```
