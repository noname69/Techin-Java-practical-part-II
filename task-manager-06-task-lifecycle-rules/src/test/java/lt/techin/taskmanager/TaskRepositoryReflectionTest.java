package lt.techin.taskmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskRepositoryReflectionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void taskUsesStatusEnumAndRepositorySearchesByStatus() throws Exception {
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'.");
        Class<?> repositoryType = loadRequiredClass("lt.techin.taskmanager.repository.TaskRepository", "Could not find interface 'lt.techin.taskmanager.repository.TaskRepository'.");

        assertNotNull(taskType.getAnnotation(Entity.class), "Task should remain a JPA entity.");

        Field idField = taskType.getDeclaredField("id");
        assertNotNull(idField.getAnnotation(Id.class), "Task.id should be annotated with @Id.");
        assertNotNull(idField.getAnnotation(GeneratedValue.class), "Task.id should be annotated with @GeneratedValue.");

        Field statusField = taskType.getDeclaredField("status");
        Enumerated enumerated = statusField.getAnnotation(Enumerated.class);
        assertNotNull(enumerated, "Task.status should be annotated with @Enumerated.");
        assertEquals(EnumType.STRING, enumerated.value(), "Task.status should be stored as a readable enum string.");

        taskType.getDeclaredField("completedAt");

        assertTrue(JpaRepository.class.isAssignableFrom(repositoryType), "TaskRepository should extend JpaRepository.");
        repositoryType.getMethod("findByStatus", statusType);
    }

    @Test
    void repositoryPersistsLifecycleState() throws Exception {
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'.");
        Class<?> repositoryType = loadRequiredClass("lt.techin.taskmanager.repository.TaskRepository", "Could not find interface 'lt.techin.taskmanager.repository.TaskRepository'.");

        Object repository = applicationContext.getBean(repositoryType);
        Method saveMethod = repositoryType.getMethod("save", Object.class);
        Method findByStatusMethod = repositoryType.getMethod("findByStatus", statusType);

        Object todo = Enum.valueOf((Class<Enum>) statusType.asSubclass(Enum.class), "TODO");
        Object done = Enum.valueOf((Class<Enum>) statusType.asSubclass(Enum.class), "DONE");

        Object first = taskType.getDeclaredConstructor().newInstance();
        taskType.getMethod("setTitle", String.class).invoke(first, "One");
        taskType.getMethod("setDescription", String.class).invoke(first, "First task");
        taskType.getMethod("setStatus", statusType).invoke(first, todo);
        taskType.getMethod("setDueDate", LocalDate.class).invoke(first, LocalDate.of(2099, 5, 1));

        Object second = taskType.getDeclaredConstructor().newInstance();
        taskType.getMethod("setTitle", String.class).invoke(second, "Two");
        taskType.getMethod("setDescription", String.class).invoke(second, "Second task");
        taskType.getMethod("setStatus", statusType).invoke(second, done);
        taskType.getMethod("setDueDate", LocalDate.class).invoke(second, LocalDate.of(2099, 6, 1));

        Object savedFirst = saveMethod.invoke(repository, first);
        Object savedSecond = saveMethod.invoke(repository, second);

        assertNotNull(taskType.getMethod("getId").invoke(savedFirst), "Persisting a task should generate an id.");
        assertNotNull(taskType.getMethod("getId").invoke(savedSecond), "Persisting another task should also generate an id.");

        List<?> doneTasks = (List<?>) findByStatusMethod.invoke(repository, done);
        assertEquals(1, doneTasks.size(), "findByStatus(DONE) should return only done tasks.");
        assertEquals("Two", taskType.getMethod("getTitle").invoke(doneTasks.get(0)), "findByStatus(DONE) should return the persisted done task.");
    }

    private Class<?> loadRequiredClass(String fqcn, String message) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException exception) {
            fail(message);
            return null;
        }
    }
}
