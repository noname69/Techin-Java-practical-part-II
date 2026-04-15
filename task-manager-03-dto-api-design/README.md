# Task Manager 03 DTOs and API Design

## Overview
Refactor your Task 02 project so that the API no longer exposes the internal `Task` model directly.

This task starts from the full Task 02 codebase. In other words, the first step is: take your finished Task 02 project and copy all of it before you begin the refactoring work.

This starter already begins from that Task 02 structure:
- controller
- service
- repository
- model
- exception

Your job is to add DTOs and refactor the controller boundary.

## What You Learn
- why internal model and API contract should be separated
- how request DTOs and response DTOs improve API design
- how API contracts can evolve without changing internal classes everywhere
- why exposing internal objects directly is risky
- how to do manual mapping in a clear and maintainable way

## Package Structure
Keep the Task 02 packages:
- `lt.techin.taskmanager.controller`
- `lt.techin.taskmanager.service`
- `lt.techin.taskmanager.repository`
- `lt.techin.taskmanager.model`
- `lt.techin.taskmanager.exception`

Add a new package:
- `lt.techin.taskmanager.dto`

## What You Must Create
Create these DTO types:
- `CreateTaskRequest`
- `UpdateTaskRequest`
- `TaskResponse`

For this task, records are recommended for DTOs. They are a good fit because DTOs represent API data, not mutable application state.

Create a dedicated manual mapper:
- `TaskMapper`

## What Must Stay The Same
Keep using the internal `Task` model in:
- service layer
- repository layer

Keep the same endpoints:
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `GET /api/tasks/search?done=true`
- `POST /api/tasks`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}/done?value=true`
- `DELETE /api/tasks/{id}`

Keep the same status codes:
- `200 OK`
- `201 Created`
- `204 No Content`
- `404 Not Found`

Keep `TaskNotFoundException` handling from Task 02.

## Step 1: Start From Task 02 Code
This task assumes you already have all Task 02 code copied first.

That means you should begin with:
- `Task`
- layered packages
- repository and service
- `TaskController`
- `TaskNotFoundException`

Do not rebuild the whole project from scratch. This task is about refactoring the API boundary, not rebuilding layers.

## Step 2: Create DTO Types
In package `lt.techin.taskmanager.dto`, create:

### `CreateTaskRequest`
Fields:
- `title`
- `description`

### `UpdateTaskRequest`
Fields:
- `title`
- `description`
- `done`

### `TaskResponse`
Fields:
- `id`
- `title`
- `description`
- `done`

Recommended approach:
- use Java `record` for all three DTOs
- keep the internal `Task` model as a regular class

Example:
```java
public record CreateTaskRequest(String title, String description) {
}
```

## Step 3: Create TaskMapper
Create `TaskMapper`.

Use manual mapping.

Do not:
- use MapStruct
- use generated mapper libraries
- write all mapping inline inside controller methods

The mapper should contain conversion methods such as:
- request DTO to internal `Task`
- internal `Task` to `TaskResponse`
- list of internal tasks to list of response DTOs

If you use records, remember that their accessors are `title()`, `description()`, `done()`, not `getTitle()` or `isDone()`.

## Step 4: Refactor Controller Request Types
Change controller request handling so that:
- `POST /api/tasks` accepts `CreateTaskRequest`
- `PUT /api/tasks/{id}` accepts `UpdateTaskRequest`

`PATCH /api/tasks/{id}/done?value=true` can stay query-parameter based.

## Step 5: Refactor Controller Response Types
Change controller return types so that:
- `GET` endpoints return `TaskResponse`
- list endpoints return `List<TaskResponse>`
- `POST`, `PUT`, and `PATCH` return `TaskResponse`

The controller should no longer return internal `Task` objects directly.

## Step 6: Keep Mapping At The API Boundary
Use DTOs in the controller only.

The intended design for this task is:
- controller works with DTOs
- mapper converts between DTOs and `Task`
- service works with `Task`
- repository works with `Task`

Do not move DTOs into repository or service contracts in this task.

## Rules
- do not add validation annotations
- do not add MapStruct
- do not add Lombok mapping shortcuts
- do not change the public tests
- keep business behavior from Task 02
- keep `done = false` on create if that rule already exists in service

## How To Work
1. Start from the full Task 02 codebase.
2. Create the `dto` package.
3. Create `CreateTaskRequest`, `UpdateTaskRequest`, and `TaskResponse`, preferably as records.
4. Create `TaskMapper`.
5. Refactor controller request bodies to use DTOs.
6. Refactor controller return types to use DTOs.
7. Keep service and repository on the internal `Task` model.
8. Run tests and fix failing cases.

## Run Locally
```bash
mvn test
```

## Acceptance Checks
- the project builds with Maven
- DTO classes exist
- `TaskMapper` exists
- controller methods use DTOs where required
- service and repository still work with internal `Task`
- the API still returns the correct status codes
