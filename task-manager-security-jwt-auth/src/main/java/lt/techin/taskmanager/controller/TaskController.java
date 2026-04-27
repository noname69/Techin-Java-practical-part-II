package lt.techin.taskmanager.controller;

import jakarta.validation.Valid;
import lt.techin.taskmanager.dto.TaskMapper;
import lt.techin.taskmanager.dto.TaskPageResponse;
import lt.techin.taskmanager.dto.TaskResponse;
import lt.techin.taskmanager.dto.UpdateTaskRequest;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
import lt.techin.taskmanager.service.DefaultTaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final DefaultTaskService defaultTaskService;

    public TaskController(DefaultTaskService defaultTaskService) {
        this.defaultTaskService = defaultTaskService;
    }

    // GET all
    @GetMapping
    public TaskPageResponse getAll(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) LocalDate dueBefore,
            @PageableDefault(sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        Page<Task> page = defaultTaskService.search(status, projectId, assigneeId, dueBefore, pageable);
        return TaskMapper.toPageResponse(page);

    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(TaskMapper.toResponse(defaultTaskService.getById(id)));
    }

    // SEARCH by status
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchByStatus(@RequestParam TaskStatus status) {
        return ResponseEntity.ok(defaultTaskService.searchByStatus(status));
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

    // UPDATE task
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTaskRequest request
    ) {
        Task taskToUpdate = TaskMapper.toTask(request, id);

        return ResponseEntity.ok(
                TaskMapper.toResponse(
                        defaultTaskService.update(id, taskToUpdate, request.getProjectId(), request.getAssigneeId())
                )
        );
    }

    // UPDATE STATUS of task
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus value
    ) {
        return ResponseEntity.ok(
                TaskMapper.toResponse(
                        defaultTaskService.updateStatus(id, value)
                )
        );
    }

    // DELETE task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        defaultTaskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}