# Task Manager 04 Validation, Dates, and Error Handling

## Overview
Before you begin this task, copy your full working code from Task 03 into this project.

This student starter does not include the previous task implementation on purpose. You should first bring over your own:
- controller
- service
- repository
- model
- dto
- exception

After that, extend the copied Task 03 code with three connected improvements:
- request validation with Bean Validation
- `LocalDate` handling in JSON
- consistent REST error responses with `@ControllerAdvice`

Your job is to keep the layered structure and DTO boundary, then add validation, `dueDate`, and global error handling.

## What You Learn
- how `@Valid` works with Spring MVC
- where validation rules should live
- how JSON strings like `"2026-04-20"` map to `LocalDate`
- how to validate dates with `@FutureOrPresent`
- why `400`, `404`, and `409` mean different things
- how to return one consistent error JSON structure

## Important Design Rule
Keep the internal `Task` model as a regular class.

DTOs are a good place to use Java records because they represent API data, not mutable internal application state.

For this task, records are recommended for:
- `CreateTaskRequest`
- `UpdateTaskRequest`
- `TaskResponse`

Example:
```java
public record CreateTaskRequest(String title, String description, LocalDate dueDate) {
}
```

If you use records, remember:
- accessor methods are `title()`, `description()`, `dueDate()`
- not `getTitle()`, `getDescription()`, `getDueDate()`

## API You Must Keep
Base path:
- `/api/tasks`

Endpoints:
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `GET /api/tasks/search?done=true`
- `POST /api/tasks`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}/done?value=true`
- `DELETE /api/tasks/{id}`

Required status codes:
- successful read/update -> `200 OK`
- successful create -> `201 Created`
- successful delete -> `204 No Content`
- validation failure -> `400 Bad Request`
- missing task -> `404 Not Found`
- illegal state change -> `409 Conflict`

## Domain And DTO Fields
Internal `Task` model:
- `id`
- `title`
- `description`
- `done`
- `dueDate`

DTOs:

### `CreateTaskRequest`
- `title`
- `description`
- `dueDate`

### `UpdateTaskRequest`
- `title`
- `description`
- `done`
- `dueDate`

### `TaskResponse`
- `id`
- `title`
- `description`
- `done`
- `dueDate`

Use `LocalDate` for `dueDate`.

## JSON Examples
Valid create request:
```json
{
  "title": "Prepare Spring lecture",
  "description": "Validation and records",
  "dueDate": "2026-04-20"
}
```

Successful response:
```json
{
  "id": 1,
  "title": "Prepare Spring lecture",
  "description": "Validation and records",
  "done": false,
  "dueDate": "2026-04-20"
}
```

Validation error response:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "fieldErrors": [
    {
      "field": "title",
      "message": "must not be blank"
    },
    {
      "field": "dueDate",
      "message": "must be a date in the present or future"
    }
  ]
}
```

Not found response:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Task with id 99 was not found."
}
```

Conflict response:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Task with id 1 is already done."
}
```

## Validation Rules
Add validation to request DTOs only.

### `title`
- `@NotBlank`
- `@Size(min = 3, max = 100)`

### `description`
- `@Size(max = 1000)`

### `dueDate`
- `@NotNull`
- `@FutureOrPresent`

Do not put these validation annotations on the internal `Task` model in this task.

## HTTP Meaning
Use these meanings consistently:

- `400 Bad Request`
  The client sent invalid input, for example blank title or past `dueDate`.
- `404 Not Found`
  The task id does not exist.
- `409 Conflict`
  The request is structurally valid, but breaks a business rule.

For this task, the business conflict rule is:
- if a task is already `done=true`, calling `PATCH /api/tasks/{id}/done?value=true` again must return `409 Conflict`

## How `LocalDate` Works In JSON
Spring Boot with Jackson can map ISO date strings directly to `LocalDate`.

That means JSON like:
```json
{
  "dueDate": "2026-04-20"
}
```

can become:
```java
LocalDate dueDate
```

Use ISO format:
- `yyyy-MM-dd`

## What You Need To Implement
1. Copy your full Task 03 code into this project first.
2. Add `spring-boot-starter-validation` to `pom.xml`.
3. Add `dueDate` to the internal `Task` model.
4. Update or recreate DTOs so they include `dueDate`.
5. Update `TaskMapper` for the new DTO and model fields.
6. Add validation annotations to request DTOs.
7. Add `@Valid` to controller request body parameters.
8. Create `IllegalTaskStateException`.
9. Create `GlobalExceptionHandler` with `@ControllerAdvice`.
10. Return one consistent error JSON structure.

## Suggested Step-By-Step Work
### Step 1: Add validation dependency
Before this step, make sure you already copied your working Task 03 classes into this project.

Add:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### Step 2: Add `dueDate` to `Task`
Update the internal model and all constructors/getters/setters you need.

### Step 3: Update DTOs
Recommended record shapes:
```java
public record CreateTaskRequest(String title, String description, LocalDate dueDate) {
}

public record UpdateTaskRequest(String title, String description, boolean done, LocalDate dueDate) {
}

public record TaskResponse(Long id, String title, String description, boolean done, LocalDate dueDate) {
}
```

### Step 4: Update `TaskMapper`
Map:
- request DTO -> `Task`
- `Task` -> `TaskResponse`
- `List<Task>` -> `List<TaskResponse>`

### Step 5: Add validation annotations
Put them on request DTO components or fields only.

### Step 6: Add `@Valid`
Use it on:
- `POST /api/tasks`
- `PUT /api/tasks/{id}`

### Step 7: Add business exception
Create `IllegalTaskStateException`.

Use it when:
- a task is already done
- and the client tries to set `done=true` again

### Step 8: Add global exception handling
Create `GlobalExceptionHandler` with `@ControllerAdvice`.

Handle:
- validation errors
- `TaskNotFoundException`
- `IllegalTaskStateException`

### Step 9: Return one error JSON shape
All errors should include:
- `status`
- `error`
- `message`

Validation errors should also include:
- `fieldErrors`

Each field error should include:
- `field`
- `message`

## Invalid Request Examples You Should Handle
Blank title:
```json
{
  "title": "   ",
  "description": "Bad request",
  "dueDate": "2026-04-20"
}
```

Too short title:
```json
{
  "title": "Hi",
  "description": "Too short",
  "dueDate": "2026-04-20"
}
```

Too long title:
```json
{
  "title": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "description": "Too long",
  "dueDate": "2026-04-20"
}
```

Too long description:
```json
{
  "title": "Valid title",
  "description": "Use a string longer than 1000 characters here",
  "dueDate": "2026-04-20"
}
```

Missing due date:
```json
{
  "title": "Valid title",
  "description": "Missing date"
}
```

Past due date:
```json
{
  "title": "Valid title",
  "description": "Past date",
  "dueDate": "2020-01-01"
}
```

## Rules
- keep DTOs at the controller boundary
- keep service and repository on internal `Task`
- do not use `ProblemDetail`
- do not expose stack traces in JSON
- do not expose raw exception class names in JSON
- do not move DTOs into service or repository contracts
- do not remove the existing endpoints

## Run Locally
```bash
mvn test
```

## Acceptance Checks
- valid create and update requests succeed with `dueDate`
- `GET` responses include `dueDate`
- invalid title returns `400`
- invalid description returns `400`
- missing or past `dueDate` returns `400`
- unknown task id returns `404`
- repeated `done=true` patch returns `409`
- all error responses use the required JSON structure
