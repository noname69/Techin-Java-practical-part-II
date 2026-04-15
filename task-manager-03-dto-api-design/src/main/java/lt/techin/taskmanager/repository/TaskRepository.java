package lt.techin.taskmanager.repository;

import lt.techin.taskmanager.model.Task;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository {

    List<Task> findAll();

    Optional<Task> findById(Long id);

    List<Task> findByDone(boolean done);

    Task save(Task task);

    boolean deleteById(Long id);
}
