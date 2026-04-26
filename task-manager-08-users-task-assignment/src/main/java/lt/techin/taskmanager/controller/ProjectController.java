package lt.techin.taskmanager.controller;

import jakarta.validation.Valid;
import lt.techin.taskmanager.dto.*;
import lt.techin.taskmanager.model.Project;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
import lt.techin.taskmanager.service.ProjectService;
import lt.techin.taskmanager.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    // GET /api/projects
    @GetMapping
    public List<ProjectResponse> getAll() {
        return ProjectMapper.toResponseList(projectService.getAll());
    }

    // GET /api/projects/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ProjectMapper.toResponse(projectService.getById(id)));
    }

    // GET /api/projects/{id}/tasks
    @GetMapping("/{id}/tasks")
    public List<TaskResponse> getTasks(
            @PathVariable Long id,
            @RequestParam(required = false)TaskStatus status
            ) {
        return taskService.getTasksByProjectId(id);
    }

    // POST /api/projects
    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody CreateProjectRequest request
            ) {

        Project project = ProjectMapper.toProject(request);
        Project created = projectService.create(project);

        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectMapper.toResponse(created));
    }

    // POST /api/projects/{id}/tasks
    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @RequestBody @Valid CreateTaskRequest request
    ) {
        Task task = TaskMapper.toTask(request);

        Task created = taskService.create(projectId, task, request.getAssigneeId());

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
        Project updatedProject = projectService.update(id, projectToUpdate);

        return ResponseEntity.ok(ProjectMapper.toResponse(updatedProject));
    }

    // PATCH /api/projects/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> patch(
            @PathVariable Long id,
            @Valid @RequestBody PatchProjectRequest request
    ) {
        Project updated = projectService.updateArchived(id, request.archived());
        return ResponseEntity.ok(ProjectMapper.toResponse(updated));
    }

    // DELETE /api/projects/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable long id
    ) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }




}
