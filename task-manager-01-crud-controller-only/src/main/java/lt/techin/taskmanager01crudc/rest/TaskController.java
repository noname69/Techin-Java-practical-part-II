package lt.techin.taskmanager01crudc.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        System.out.println("GET LIST");

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        System.out.println("GET BY ID");

        Task task = tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(task);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchByDone(@RequestParam boolean done) {
        System.out.println("SEARCH");

        List<Task> founded = tasks.stream()
                .filter(t -> t.isDone() == done)
                .toList();

        return ResponseEntity.ok(founded);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        System.out.println("POST");
        System.out.println(task);

        task.setId(idCounter.getAndIncrement());
        tasks.add(task);

        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task updatedTask
    ) {
        System.out.println("PUT");
        System.out.println(updatedTask);

        Optional<Task> optionalTask = tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();

        if (optionalTask.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = optionalTask.get();

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setDone(updatedTask.isDone());

        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/done")
    public ResponseEntity<Task> updateDone(
            @PathVariable Long id,
            @RequestParam boolean value
    ) {
        System.out.println("PATCH");

        Optional<Task> optionalTask = tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();

        if (optionalTask.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = optionalTask.get();
        task.setDone(value);

        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        System.out.println("DELETE");

        boolean removed = tasks.removeIf(t -> t.getId().equals(id));

        if (!removed) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}