package lt.techin.taskmanager.service;

import lt.techin.taskmanager.dto.TaskMapper;
import lt.techin.taskmanager.dto.TaskResponse;
import lt.techin.taskmanager.dto.UpdateTaskRequest;
import lt.techin.taskmanager.exception.*;
import lt.techin.taskmanager.model.Project;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
import lt.techin.taskmanager.model.User;
import lt.techin.taskmanager.repository.ProjectRepository;
import lt.techin.taskmanager.repository.TaskRepository;
import lt.techin.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static lt.techin.taskmanager.model.TaskStatus.DONE;
import static lt.techin.taskmanager.model.TaskStatus.TODO;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public Task getById(Long id) {
        Optional<Task> taskOpt = taskRepository.findById(id);

        if (taskOpt.isPresent()) {
            return taskOpt.get();
        }

        throw new TaskNotFoundException(id);
    }

    public List<Task> searchByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

//    public Task create(Long id, Task task) {
//        Project project = projectRepository.findById(id)
//                .orElseThrow(() -> new ProjectNotFoundException(id));
//
//        if (project.isArchived()) {
//            throw new ArchivedProjectException(id);
//        }
//
//        task.setProject(project);
//        task.setStatus(TaskStatus.TODO);

//        if (task.getAssignee() != null) {
//            User user = userRepository.findById(task.getAssignee())
//                    .orElseThrow(() -> new UserNotFoundException(task.getAssigneeId()));
//
//            task.setAssignee(user);
//        }
//
//        return taskRepository.save(task);
//    }

    public Task create(Long projectId, Task task, Long assigneeId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (project.isArchived()) {
            throw new ArchivedProjectException(projectId);
        }

        if (assigneeId != null) {
            User user = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new UserNotFoundException(assigneeId));

            task.setAssignee(user);
        } else {
            task.setAssignee(null);
        }

        task.setProject(project);
        task.setStatus(TaskStatus.TODO);

        return taskRepository.save(task);
    }

    public Task update(Long id, Task task, Long projectId, Long assigneeId) {

        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (project.isArchived()) {
            throw new ArchivedProjectException(projectId);
        }

        if (existing.getStatus() == TaskStatus.DONE && assigneeId != null) {
            throw new IllegalTaskStateException(
                    "Task with id " + id + " is completed and cannot be reassigned."
            );
        }

        if (assigneeId != null) {
            User user = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new UserNotFoundException(assigneeId));
            existing.setAssignee(user);
        } else {
            existing.setAssignee(null);
        }

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setDueDate(task.getDueDate());
        existing.setProject(project);

        applyStatusChange(existing, task.getStatus());

        return taskRepository.save(existing);
    }

    private void applyStatusChange(Task task, TaskStatus newStatus) {

        TaskStatus current = task.getStatus();

        if (current == TODO && newStatus == DONE) {
            task.setStatus(DONE);
            task.setCompletedAt(LocalDateTime.now());
            return;
        }

        if (task.getStatus() == DONE && newStatus == TODO) {
            throw new IllegalTaskStateException(
                    "Task with id " + task.getId() + " cannot move from DONE back to TODO."
            );
        }

        if (task.getStatus() == DONE && newStatus == DONE) {
            throw new IllegalTaskStateException(
                    "Task with id " + task.getId() + " is already done."
            );
        }
    }

    public Task updateStatus(Long id, TaskStatus newStatus) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        applyStatusChange(task, newStatus);

        return taskRepository.save(task);
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }

        taskRepository.deleteById(id);
    }

    public List<TaskResponse> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

}
