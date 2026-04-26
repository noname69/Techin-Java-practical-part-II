package lt.techin.taskmanager.controller;

import jakarta.validation.Valid;
import lt.techin.taskmanager.dto.CreateTaskRequest;
import lt.techin.taskmanager.dto.TaskMapper;
import lt.techin.taskmanager.dto.TaskResponse;
import lt.techin.taskmanager.dto.UpdateTaskRequest;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
import lt.techin.taskmanager.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET all
    @GetMapping
    public List<TaskResponse> getAll() {
        return TaskMapper.toResponseList(taskService.getAll());
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(TaskMapper.toResponse(taskService.getById(id)));
    }

    // SEARCH by status
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchByStatus(@RequestParam TaskStatus status) {
        return ResponseEntity.ok(taskService.searchByStatus(status));
    }

    // CREATE new task
//    @PostMapping
//    public ResponseEntity<TaskResponse> create(
//            @Valid @RequestBody CreateTaskRequest request) {
//        Task task = TaskMapper.toTask(request);
//        Task created = taskService.create(task);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(TaskMapper.toResponse(created));
//    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTaskRequest request
    ) {
        Task taskToUpdate = TaskMapper.toTask(request, id);

        return ResponseEntity.ok(
                TaskMapper.toResponse(
                        taskService.update(id, taskToUpdate, request.getProjectId(), request.getAssigneeId())
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus value
    ) {
        return ResponseEntity.ok(
                TaskMapper.toResponse(
                        taskService.updateStatus(id, value)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}