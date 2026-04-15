# Task Manager 01 Crud Controller Only

## Overview
Build a small Spring MVC REST API for managing tasks in memory.

This is a controller-only task:
- no database
- no repository
- no service
- no starter `Task` class
- no starter `TaskController` class

You must create the missing classes yourself.

## What You Learn
- difference between HTTP methods
- how request data enters the controller
- how `@PathVariable`, `@RequestParam`, and `@RequestBody` work
- how JSON becomes Java objects
- how Java objects become JSON responses
- why correct HTTP status codes matter

## What You Must Create
Create the missing source code in package `lt.techin.taskmanager01crudc.rest`.

At minimum, you need:
- a `Task` class
- a `TaskController` class

You may add more classes if you want, but keep the task controller-only. Do not introduce a database, repository, or service layer.

## Step 1: Create the Task Model
Create a Java class named `Task`.

It must have these fields:
- `id`
- `title`
- `description`
- `done`

Use types that make sense for a simple REST task:
- `id` should represent a numeric identifier
- `title` and `description` should store text
- `done` should store true/false state

Make sure Spring can serialize and deserialize this class as JSON.

## Step 2: Create the Controller
Create a class named `TaskController`.

It should:
- be a REST controller
- handle requests under `/api/tasks`
- store all data in memory

Keep the state inside the controller itself.

## Step 3: Add In-Memory Storage
Inside the controller:
- keep a `List<Task>` for storing tasks
- keep a simple ID counter

When a new task is created:
- assign the next ID
- save it in the list

## Step 4: Implement the Endpoints
Implement these endpoints.

### `GET /api/tasks`
- return all tasks
- response status must be `200 OK`

### `GET /api/tasks/{id}`
- read the task ID from the URL using `@PathVariable`
- return the matching task
- if the task does not exist, return `404 Not Found`

### `GET /api/tasks/search?done=true`
- read the `done` value from the query string using `@RequestParam`
- return only tasks that match that `done` value
- response status must be `200 OK`

### `POST /api/tasks`
- read JSON from the request body using `@RequestBody`
- create a new task
- store it in the in-memory list
- return the created task
- response status must be `201 Created`

### `PUT /api/tasks/{id}`
- read the task ID from the URL
- read updated task data from the request body
- replace the existing task values
- return the updated task
- if the task does not exist, return `404 Not Found`
- response status for success must be `200 OK`

### `PATCH /api/tasks/{id}/done?value=true`
- read the task ID from the URL
- read the new done value from the query string using `@RequestParam`
- update only the `done` field
- return the updated task
- if the task does not exist, return `404 Not Found`
- response status for success must be `200 OK`

### `DELETE /api/tasks/{id}`
- remove the task from the list
- if the task exists, return `204 No Content`
- if the task does not exist, return `404 Not Found`

## JSON Shape
Your API should work with JSON objects shaped like this:

```json
{
  "id": 1,
  "title": "Write README",
  "description": "Finish the assignment brief",
  "done": false
}
```

For create requests, tests may send JSON without an `id`. Your code should assign the ID itself.

## Required Status Codes
Your API must return the correct status codes:
- `200 OK` for successful reads and updates
- `201 Created` for successful creation
- `204 No Content` for successful deletion
- `404 Not Found` for unknown task IDs

If the status code is wrong, the tests should fail even if the JSON body is correct.

## Rules
- Keep the provided package structure unchanged.
- Do not change the public tests.
- Do not add a database.
- Do not add a repository.
- Do not add a service layer.
- Keep the solution simple and focused on controller logic.

## How To Work
1. Create the `Task` class.
2. Create the `TaskController` class.
3. Add in-memory storage and ID generation.
4. Implement each endpoint one by one.
5. Run tests and fix failing cases.
6. Check that your status codes are correct.

## Run Locally
```bash
mvn test
```

## Acceptance Checks
- The project builds with Maven.
- The public API matches the tests.
- JSON request and response handling works correctly.
- The API returns the required status codes.
