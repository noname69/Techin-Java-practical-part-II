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

    @GetMapping
    public List<ProjectResponse> getAll() {
        return ProjectMapper.toResponseList(projectService.getAll());
    }

    @GetMapping("/{id}")
    ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ProjectMapper.toResponse(projectService.getById(id)));
    }

//    @GetMapping("/{id}/tasks")
//    public ResponseEntity<List<Task>> getProjectTasks(
//            @PathVariable Long id,
//            @RequestParam(required = false)TaskStatus status
//            ) {
//        if(status != null) {
//            return null;
//        }
//
//        return ResponseEntity.ok(taskService.getByProject(id));
//    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @RequestBody @Valid CreateProjectRequest request
    ) {
        Project project = ProjectMapper.toProject(request);
        Project created = projectService.create(project);

        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProjectRequest request
    ) {

        Project projectToUpdate = ProjectMapper.toProject(request, id);

        return ResponseEntity.ok(
                ProjectMapper.toResponse(
                        projectService.update(id, projectToUpdate)
                )
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> patch(
            @PathVariable Long id,
            @RequestBody PatchProjectRequest request
    ) {
        Project updated = projectService.patchArchived(id, request.archived());
        return ResponseEntity.ok(ProjectMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @RequestBody @Valid CreateTaskRequest request
    ) {
        Task task = TaskMapper.toTask(request);

        Task created = taskService.create(projectId, task);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TaskMapper.toResponse(created));
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<Task>> getTasks(
            @PathVariable Long id,
            @RequestParam(required = false) TaskStatus status
    ) {
        List<Task> result;

        if (status == null) {
            result = taskService.getByProject(id);
        } else {
            result = taskService.getByProjectAndStatus(id, status);
        }

        return ResponseEntity.ok(result);
    }

}
