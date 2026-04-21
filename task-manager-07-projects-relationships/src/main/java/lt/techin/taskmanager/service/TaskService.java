package lt.techin.taskmanager.service;

import lt.techin.taskmanager.exception.ArchivedProjectException;
import lt.techin.taskmanager.exception.IllegalTaskStateException;
import lt.techin.taskmanager.exception.ProjectNotFoundException;
import lt.techin.taskmanager.exception.TaskNotFoundException;
import lt.techin.taskmanager.model.Project;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
import lt.techin.taskmanager.repository.ProjectRepository;
import lt.techin.taskmanager.repository.TaskRepository;
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

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
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

    public Task create(Long projectId, Task task) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (project.isArchived()) {
            throw new ArchivedProjectException(projectId);
        }

        task.setProject(project);
        task.setStatus(TaskStatus.TODO);
        task.setCompletedAt(null);

        return taskRepository.save(task);
    }


    public Task update(Long id, Task updatedTask) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDueDate(updatedTask.getDueDate());

        if (updatedTask.getProject() != null) {

            Long newProjectId = updatedTask.getProject().getId();

            Project newProject = projectRepository.findById(newProjectId)
                    .orElseThrow(() -> new ProjectNotFoundException(newProjectId));

            if (newProject.isArchived()) {
                throw new ArchivedProjectException(
                        newProjectId
                );
            }

            existing.setProject(newProject);
        }

        if (updatedTask.getStatus() != null) {
            applyStatusChange(existing, updatedTask.getStatus());
        }

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

    public List<Task> getByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }


    public List<Task> getByProjectAndStatus(Long projectId, TaskStatus status) {
        return taskRepository.findByProjectIdAndStatus(projectId, status);
    }
}
