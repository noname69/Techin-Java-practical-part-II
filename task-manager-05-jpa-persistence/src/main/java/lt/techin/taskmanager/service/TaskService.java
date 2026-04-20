package lt.techin.taskmanager.service;

import lt.techin.taskmanager.exception.IllegalTaskStateException;
import lt.techin.taskmanager.exception.TaskNotFoundException;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public List<Task> searchByDone(boolean done) {
        return taskRepository.findByDone(done);
    }

    public Task create(Task task) {
        task.setDone(false);

        return taskRepository.save(task);
    }

    public Task update(Long id, Task updatedTask) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDone(updatedTask.isDone());
        existing.setDueDate(updatedTask.getDueDate());

        return taskRepository.save(existing);
    }

    public Task updateDone(Long id, boolean value) {
        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            throw new TaskNotFoundException(id);
        }

        Task task = optionalTask.get();

        if (task.isDone() && value) {
            throw new IllegalTaskStateException(task.getId());
        }

        task.setDone(value);

        return taskRepository.save(task);
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }

        taskRepository.deleteById(id);
    }

}
