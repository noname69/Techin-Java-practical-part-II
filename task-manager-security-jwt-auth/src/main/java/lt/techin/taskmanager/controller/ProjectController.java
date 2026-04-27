package lt.techin.taskmanager.controller;

import jakarta.validation.Valid;
import lt.techin.taskmanager.dto.*;
import lt.techin.taskmanager.model.Project;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
import lt.techin.taskmanager.service.DefaultProjectService;
import lt.techin.taskmanager.service.DefaultTaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final DefaultProjectService defaultProjectService;
    private final DefaultTaskService defaultTaskService;

    public ProjectController(DefaultProjectService defaultProjectService, DefaultTaskService defaultTaskService) {
        this.defaultProjectService = defaultProjectService;
        this.defaultTaskService = defaultTaskService;
    }

    // GET /api/projects
    @GetMapping
    public List<ProjectResponse> getAll() {
        return ProjectMapper.toResponseList(defaultProjectService.getAll());
    }

    // GET /api/projects/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ProjectMapper.toResponse(defaultProjectService.getById(id)));
    }

    // GET /api/projects/{id}/tasks
//    @GetMapping("/{id}/tasks")
//    public List<TaskResponse> getTasks(
//            @PathVariable Long id,
//            @RequestParam(required = false)TaskStatus status
//            ) {
//        return taskService.getTasksByProjectId(id);
//    }

    @GetMapping("/{id}/tasks")
    public TaskPageResponse getTasks(
            @PathVariable Long id,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) LocalDate dueBefore,
            @PageableDefault(sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Task> page = defaultTaskService.searchByProject(id, status, assigneeId, dueBefore, pageable);
        return TaskMapper.toPageResponse(page);
    }

    // POST /api/projects
    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody CreateProjectRequest request
    ) {

        Project project = ProjectMapper.toProject(request);
        Project created = defaultProjectService.create(project);

        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectMapper.toResponse(created));
    }

    // POST /api/projects/{id}/tasks
    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @RequestBody @Valid CreateTaskRequest request
    ) {
        Task task = TaskMapper.toTask(request);

        Task created = defaultTaskService.create(projectId, task, request.getAssigneeId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TaskMapper.toResponse(created));
    }

    // PUT /api/projects/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        Project projectToUpdate = ProjectMapper.toProject(request, id);
        Project updatedProject = defaultProjectService.update(id, projectToUpdate);

        return ResponseEntity.ok(ProjectMapper.toResponse(updatedProject));
    }

    // PATCH /api/projects/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> patch(
            @PathVariable Long id,
            @Valid @RequestBody PatchProjectRequest request
    ) {
        Project updated = defaultProjectService.updateArchived(id, request.archived());
        return ResponseEntity.ok(ProjectMapper.toResponse(updated));
    }

    // DELETE /api/projects/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable long id
    ) {
        defaultProjectService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
