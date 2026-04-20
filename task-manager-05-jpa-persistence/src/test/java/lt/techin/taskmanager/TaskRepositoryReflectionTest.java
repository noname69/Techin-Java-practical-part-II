package lt.techin.taskmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskRepositoryReflectionTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void taskIsJpaEntityAndRepositoryExtendsJpaRepository() throws Exception {
        assertNotNull(Task.class.getAnnotation(Entity.class), "Task should be annotated with @Entity.");

        Field idField = Task.class.getDeclaredField("id");
        assertNotNull(idField.getAnnotation(Id.class), "Task.id should be annotated with @Id.");
        assertNotNull(idField.getAnnotation(GeneratedValue.class), "Task.id should be annotated with @GeneratedValue.");

        assertTrue(
                JpaRepository.class.isAssignableFrom(TaskRepository.class),
                "TaskRepository should extend JpaRepository<Task, Long>."
        );
    }

    @Test
    void repositoryPersistsTasksGeneratesIdsAndFiltersByDone() {
        Task first = new Task(null, "One", "First task", false, LocalDate.of(2099, 5, 1));
        Task second = new Task(null, "Two", "Second task", true, LocalDate.of(2099, 6, 1));

        Task savedFirst = taskRepository.save(first);
        Task savedSecond = taskRepository.save(second);

        assertNotNull(savedFirst.getId(), "Saving a new task should generate a database id.");
        assertNotNull(savedSecond.getId(), "Saving another new task should also generate a database id.");
        assertTrue(savedSecond.getId() > savedFirst.getId(), "Generated ids should increase for later inserted tasks in this test.");

        List<Task> all = taskRepository.findAll();
        List<Task> doneTasks = taskRepository.findByDone(true);

        assertEquals(2, all.size(), "TaskRepository.findAll() should return persisted tasks from the database.");
        assertEquals(1, doneTasks.size(), "TaskRepository.findByDone(true) should return only tasks whose done field is true.");
        assertEquals("Two", doneTasks.get(0).getTitle(), "TaskRepository.findByDone(true) should return the persisted done task.");
        assertEquals(LocalDate.of(2099, 6, 1), doneTasks.get(0).getDueDate(), "Repository should preserve dueDate when persisting tasks.");
    }
}
