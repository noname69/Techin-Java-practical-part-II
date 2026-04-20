package lt.techin.taskmanager.repository;

import lt.techin.taskmanager.model.Task;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public List<Task> findAll() {
        System.out.println("GET LIST");
        return new ArrayList<>(tasks);
    }

    @Override
    public Optional<Task> findById(Long id) {
        System.out.println("GET BY ID");

        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Task> findByDone(boolean done) {
        System.out.println("SEARCH");

        return tasks.stream()
                .filter(t -> t.isDone() == done)
                .toList();

    }

    public Task save(Task task) {
        System.out.println("ADD / UPDATE");

        if (task.getId() == null) {
            task.setId(idCounter.getAndIncrement());
            tasks.add(task);
            return task;
        }

        for (Task existingTask : tasks) {
            if (existingTask.getId().equals(task.getId())) {
                existingTask.setTitle(task.getTitle());
                existingTask.setDescription(task.getDescription());
                existingTask.setDone(task.isDone());
                existingTask.setDueDate(task.getDueDate());
                return existingTask;
            }
        }

        return null;
    }

    @Override
    public boolean deleteById(Long id) {
        System.out.println("DELETE");

        return tasks.removeIf(t -> t.getId().equals(id));
    }
}
