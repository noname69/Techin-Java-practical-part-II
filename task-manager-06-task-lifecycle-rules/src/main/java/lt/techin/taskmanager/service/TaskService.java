package lt.techin.taskmanager.service;

import lt.techin.taskmanager.exception.IllegalTaskStateException;
import lt.techin.taskmanager.exception.TaskNotFoundException;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
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

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
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

    public Task create(Task task) {
        task.setStatus(TODO);

        return taskRepository.save(task);
    }

    public Task update(Long id, Task updatedTask) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDueDate(updatedTask.getDueDate());

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

}
