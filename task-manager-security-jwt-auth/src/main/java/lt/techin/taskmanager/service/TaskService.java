package lt.techin.taskmanager.service;

import lt.techin.taskmanager.dto.TaskResponse;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {

    List<Task> getAll();

    Task getById(Long id);

    List<Task> searchByStatus(TaskStatus status);

    Task create(Long projectId, Task task, Long assigneeId);

    Task update(Long id, Task task, Long projectId, Long assigneeId);

    void applyStatusChange(Task task, TaskStatus newStatus);

    Task updateStatus(Long id, TaskStatus newStatus);

    void delete(Long id);

    List<TaskResponse> getTasksByProjectId(Long projectId);

    Page<Task> search(
            TaskStatus status,
            Long projectId,
            Long assigneeId,
            LocalDate dueBefore,
            Pageable pageable
    );

    Page<Task> searchByProject(
            Long projectId,
            TaskStatus status,
            Long assigneeId,
            LocalDate dueBefore,
            Pageable pageable
    );
}
