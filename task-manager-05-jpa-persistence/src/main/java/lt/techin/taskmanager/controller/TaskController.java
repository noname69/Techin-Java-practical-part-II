package lt.techin.taskmanager.controller;

import jakarta.validation.Valid;
import lt.techin.taskmanager.dto.CreateTaskRequest;
import lt.techin.taskmanager.dto.TaskMapper;
import lt.techin.taskmanager.dto.TaskResponse;
import lt.techin.taskmanager.dto.UpdateTaskRequest;
import lt.techin.taskmanager.model.Task;
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

    @GetMapping
    public List<TaskResponse> getAll() {
        return TaskMapper.toResponseList(taskService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(TaskMapper.toResponse(taskService.getById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchByDone(@RequestParam boolean done) {
        return ResponseEntity.ok(taskService.searchByDone(done));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        Task task = TaskMapper.toTask(request);
        Task created = taskService.create(task);

        return ResponseEntity.status(HttpStatus.CREATED).body(TaskMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        Task taskToUpdate = TaskMapper.toTask(request, id);

        return ResponseEntity.ok(TaskMapper.toResponse(taskService.update(id, taskToUpdate)));
    }

    @PatchMapping("/{id}/done")
    public ResponseEntity<TaskResponse> updateDone(
            @PathVariable Long id,
            @Valid @RequestParam boolean value
    ) {
        return ResponseEntity.ok(TaskMapper.toResponse(taskService.updateDone(id, value)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}