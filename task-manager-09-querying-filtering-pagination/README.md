# Task Manager 09 Querying, Filtering, Sorting, and Pagination

## Overview
Before you begin this task, copy your full working code from Task 08 into this project.

This starter intentionally does not include your previous implementation. First bring over your own:
- controller
- dto
- exception
- model
- repository
- service
- JPA and H2 configuration

After that, extend the application with richer task read endpoints.

The new focus in this task is:
- query parameters for filtering
- sorting
- basic pagination
- pageable repository access
- designing collection endpoints for a richer domain

## What Must Stay The Same
Keep from Task 08:
- JPA with H2
- layered structure
- DTO boundary
- validation
- global exception handling
- project, user, and task write behavior
- task-by-id endpoints:
  - `GET /api/tasks/{id}`
  - `PUT /api/tasks/{id}`
  - `PATCH /api/tasks/{id}/status?value=DONE`
  - `DELETE /api/tasks/{id}`
- project-scoped task creation:
  - `POST /api/projects/{projectId}/tasks`
- existing business rules:
  - archived project cannot accept new tasks
  - completed task cannot be reassigned
  - duplicate user email returns `409`

This task is about richer reads, not new write behavior.

## New API Direction
### Collection endpoints
Add:
- `GET /api/tasks`
- richer `GET /api/projects/{id}/tasks`

Supported filters on `GET /api/tasks`:
- `status`
- `projectId`
- `assigneeId`
- `dueBefore`
- `page`
- `size`
- `sort`

Supported filters on `GET /api/projects/{id}/tasks`:
- `status`
- `assigneeId`
- `dueBefore`
- `page`
- `size`
- `sort`

Recommended sort format:
- `sort=field,direction`
- examples:
  - `sort=dueDate,asc`
  - `sort=title,desc`

Recommended defaults:
- `page=0`
- `size=10`
- `sort=dueDate,asc`

## Response DTO Direction
Keep `TaskResponse` for individual items.

Add one paginated wrapper DTO, for example:
- `TaskPageResponse`
  - `content`
  - `page`
  - `size`
  - `totalElements`
  - `totalPages`

Recommended shape:
- `List<TaskResponse> content`
- `int page`
- `int size`
- `long totalElements`
- `int totalPages`

Do not return raw Spring `Page` JSON directly.

## Filtering Rules
- all filters are optional
- no filters means: return all tasks paged
- project-scoped endpoint must always stay restricted to that project first
- `projectId` on global `/api/tasks` filters to one project
- `assigneeId` filters assigned tasks
- `dueBefore` returns tasks with due date before the given date
- `status` filters by `TaskStatus`
- empty filtered results still return `200 OK`

## Implementation Hints
- keep item responses as `TaskResponse`
- add a separate DTO for paginated collection responses
- use `Pageable` in service and repository logic
- keep filter-building logic in one place
- avoid creating a separate repository method for every parameter combination
- a good approach here is `JpaSpecificationExecutor<Task>`
- keep missing project handling on `/api/projects/{id}/tasks` as `404`

## JSON Examples
Global filtered query:
```http
GET /api/tasks?status=TODO&assigneeId=1&page=0&size=2&sort=dueDate,asc
```

Example response:
```json
{
  "content": [
    {
      "id": 1,
      "title": "Prepare docs",
      "description": "Write README update",
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
  ],
  "page": 0,
  "size": 2,
  "totalElements": 1,
  "totalPages": 1
}
```

Project-scoped filtered query:
```http
GET /api/projects/1/tasks?status=TODO&sort=title,desc
```

## Suggested Step-By-Step Work
1. Copy your full Task 08 code into this project.
2. Add `GET /api/tasks`.
3. Extend `GET /api/projects/{id}/tasks` with more filters and paging.
4. Add `TaskPageResponse`.
5. Update `TaskService` with paginated search methods.
6. Extend the repository for pageable filtered queries.
7. Keep filter-building logic in one place.
8. Return `TaskPageResponse` from collection endpoints.
9. Run tests and fix the failing cases.

## Rules
- do not change write endpoints
- do not return raw Spring `Page` JSON directly
- do not remove DTOs
- do not remove validation or error handling
- do not add comments or task priority in this task
- do not change the public tests

## Run Locally
```bash
mvn test
```
