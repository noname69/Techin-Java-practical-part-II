# Task Manager 02 Dependency Injection and Layers

## Overview
Refactor your Task 1 project into a layered Spring application.

You should reuse or copy your own `Task` and `TaskController` from Task 1, then reorganize the code so the controller no longer stores data or contains business logic.

This task introduces:
- Spring-managed beans
- constructor injection
- service layer
- repository abstraction
- separation of concerns

## What You Learn
- why Spring manages objects for you
- why constructor injection is preferred
- why controllers should stay thin
- how service and repository layers have different responsibilities
- how to separate HTTP logic from business logic and data access

## Package Structure
Place your code under these packages:
- `lt.techin.taskmanager.controller`
- `lt.techin.taskmanager.service`
- `lt.techin.taskmanager.repository`
- `lt.techin.taskmanager.model`
- `lt.techin.taskmanager.exception`

## What You Must Reuse
From Task 1, reuse or copy:
- your `Task` model
- your CRUD API contract
- your `/api/tasks` endpoints
- your status-code behavior

You should not build a completely different API.

## What You Must Create
Create these new types:
- `TaskService`
- `DefaultTaskService`
- `TaskRepository`
- `InMemoryTaskRepository`
- `TaskNotFoundException`

You also need to refactor `TaskController` so it delegates to the service layer.

Starter code does not include `Task`, `TaskController`, `TaskService`, or `TaskRepository`. You are expected to create them yourself, using your Task 1 code as the starting point.

## Step 1: Move Task 1 Code Into The New Structure
Copy or rewrite your Task 1 classes into the new package layout:
- move `Task` into `model`
- move `TaskController` into `controller`

Keep the same REST endpoints and response status codes.

## Step 2: Create The Repository Layer
Create:
- `TaskRepository` interface
- `InMemoryTaskRepository` implementation

## Repository Contract
Your repository should support these operations:
- `findAll`
- `findById`
- `findByDone`
- `save`
- `deleteById`

The repository is responsible for:
- storing tasks in memory
- assigning IDs
- finding tasks
- filtering by `done`
- deleting tasks

The controller must no longer keep a list or an ID counter.


## Step 3: Create The Service Layer
Create:
- `TaskService` interface
- `DefaultTaskService` implementation

## Service Contract
Your service should support these operations:
- `getAll`
- `getById`
- `searchByDone`
- `create`
- `update`
- `updateDone`
- `delete`

The service is responsible for application logic.

Required business rules:
- when creating a task, always set `done = false`
- if a task ID does not exist, throw `TaskNotFoundException`

The service should call the repository. The controller should call the service.

## Step 4: Create The Exception
Create `TaskNotFoundException` in the `exception` package.

For this task, the exception should:
- extend `RuntimeException`
- be used when a task with the given id does not exist
- include `@ResponseStatus(HttpStatus.NOT_FOUND)`

This is enough for now so that missing tasks return `404 Not Found`.

You will learn more complete exception handling later, but in this task the simple `@ResponseStatus(...)` approach is enough.

## Step 5: Refactor The Controller
Refactor `TaskController` so that it:
- stays a REST controller
- keeps the same `/api/tasks` endpoints
- receives `TaskService` through constructor injection
- delegates all application work to the service

The controller should only handle:
- request mapping
- reading request data
- returning HTTP responses
- translating missing-task situations into `404 Not Found`

The controller must not:
- store tasks
- assign IDs
- implement business rules
- access repository directly

## Step 6: Keep The Same API
Your API must still support:
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `GET /api/tasks/search?done=true`
- `POST /api/tasks`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}/done?value=true`
- `DELETE /api/tasks/{id}`

Required status codes:
- `200 OK`
- `201 Created`
- `204 No Content`
- `404 Not Found`


## Rules
- do not add a database
- do not use JPA
- keep storage in memory
- do not add DTOs
- do not change the public tests
- keep the API behavior compatible with Task 1

## How To Work
1. Copy your `Task` and `TaskController` from Task 1.
2. Move them into the new package structure.
3. Create repository abstraction and in-memory implementation.
4. Create service abstraction and implementation.
5. Create `TaskNotFoundException` and annotate it with `@ResponseStatus(HttpStatus.NOT_FOUND)`.
6. Move data logic out of the controller.
7. Move business logic into the service.
8. Inject dependencies using constructors.
9. Run tests and fix failing cases.

## Run Locally
```bash
mvn test
```

## Acceptance Checks
- the project builds with Maven
- the old Task 1 API still works
- create always returns a task with `done = false`
- missing IDs result in `404 Not Found`
- the controller delegates instead of storing data directly
