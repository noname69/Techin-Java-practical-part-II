# Task Manager 07 Projects and Relationships

## Overview
Before you begin this task, copy your full working code from Task 06 into this project.

This student starter does not include the previous task implementation on purpose. First bring over your own:
- controller
- dto
- exception
- model
- repository
- service
- JPA and H2 configuration

After that, extend the application with the first real entity relationship:
- one `Project` has many `Task`
- each `Task` belongs to one `Project`

The main lesson in this task is not basic CRUD anymore. The new focus is:
- JPA relationships
- foreign keys
- project-scoped task APIs
- using DTOs to avoid recursive JSON

## What You Learn
- how `@ManyToOne` and `@OneToMany` work
- how one entity references another in JPA
- why request and response DTOs matter more once relationships appear
- how to design endpoints around a parent-child relationship
- how service logic enforces project-related business rules

## What Must Stay The Same
Keep from Task 06:
- JPA persistence with H2
- layered structure
- DTO boundary
- validation on task DTOs
- global exception handling
- task lifecycle rules:
  - new task starts in `TODO`
  - repeated `DONE` update returns `409`
  - `DONE -> TODO` returns `409`

## New Domain Model
Add a new `Project` entity with:
- `id`
- `name`
- `description`
- `archived`

Update `Task` so it belongs to a `Project`.

Relationship direction:
- `Task` has `@ManyToOne`
- `Project` has `@OneToMany(mappedBy = "project")`

Do not return entities directly from controllers.

## API Direction
### Project endpoints
Add:
- `GET /api/projects`
- `GET /api/projects/{id}`
- `GET /api/projects/{id}/tasks`
- `POST /api/projects`
- `PUT /api/projects/{id}`
- `PATCH /api/projects/{id}`
- `DELETE /api/projects/{id}`

Optional project task filtering:
- `GET /api/projects/{id}/tasks?status=TODO`

### Task endpoints
Keep only task-by-id operations global:
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}/status?value=DONE`
- `DELETE /api/tasks/{id}`

Use project-scoped task creation:
- `POST /api/projects/{projectId}/tasks`

Do not keep the old global task collection and global task search as the main API in this task.

## DTO Direction
Use records.

### Project DTOs
Create:
- `CreateProjectRequest`
  - `name`
  - `description`
- `UpdateProjectRequest`
  - `name`
  - `description`
  - `archived`
- `PatchProjectRequest`
  - `archived`
- `ProjectResponse`
  - `id`
  - `name`
  - `description`
  - `archived`
- `ProjectSummaryResponse`
  - `id`
  - `name`
  - `archived`

### Task DTO changes
`CreateTaskRequest`:
- keep task fields only
- do not add `projectId`
- project comes from the path variable

`UpdateTaskRequest`:
- keep task fields
- add `projectId`

`TaskResponse`:
- keep task fields
- add nested `ProjectSummaryResponse project`

## JSON Examples
Create project request:
```json
{
  "name": "Spring Course",
  "description": "Main training project"
}
```

Create project response:
```json
{
  "id": 1,
  "name": "Spring Course",
  "description": "Main training project",
  "archived": false
}
```

Create task inside project:
```http
POST /api/projects/1/tasks
```

```json
{
  "title": "Write docs",
  "description": "Explain JPA relationships",
  "dueDate": "2099-05-01"
}
```

Task response:
```json
{
  "id": 1,
  "title": "Write docs",
  "description": "Explain JPA relationships",
  "status": "TODO",
  "dueDate": "2099-05-01",
  "completedAt": null,
  "project": {
    "id": 1,
    "name": "Spring Course",
    "archived": false
  }
}
```

Patch project archive state:
```http
PATCH /api/projects/1
```

```json
{
  "archived": true
}
```

## Business Rules
- task must belong to an existing project
- archived project cannot accept new tasks
- project name must be unique
- existing Task 06 lifecycle rules must still work

In practice this means:
- creating a task for missing project -> `404`
- creating a task for archived project -> `409`
- moving a task to missing project -> `404`
- moving a task to archived project -> `409`
- duplicate project name -> `409`

## Validation Direction
Add validation to project DTOs:

`name`
- `@NotBlank`
- `@Size(min = 3, max = 100)`

`description`
- `@Size(max = 1000)`

`archived`
- `@NotNull` where needed in patch and full update DTOs

Keep the existing task validation from Task 06.

## Suggested Step-By-Step Work
1. Copy your full Task 06 code into this project.
2. Create `Project` entity and `ProjectRepository`.
3. Update `Task` to reference `Project`.
4. Add project DTOs and update task DTOs.
5. Update mappers so task responses include project summary.
6. Add `ProjectService` and `ProjectController`.
7. Refactor task creation to `POST /api/projects/{projectId}/tasks`.
8. Add `GET /api/projects/{id}/tasks` and optional `status` filtering.
9. Add project-related exceptions and handle them in `@ControllerAdvice`.
10. Run tests and fix failing cases.

## Rules
- do not return entities directly from controllers
- do not expose `Project.tasks` in response DTOs
- do not remove JPA or H2
- do not remove Task 06 lifecycle behavior
- do not add many-to-many relationships
- do not change the public tests

## Run Locally
```bash
mvn test
```
