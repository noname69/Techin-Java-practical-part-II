package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskServiceReflectionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void serviceCreateStartsWithTodoAndNoCompletionTimestamp() throws Exception {
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'.");
        Class<?> serviceType = loadRequiredClass("lt.techin.taskmanager.service.TaskService", "Could not find interface 'lt.techin.taskmanager.service.TaskService'.");

        Object service = applicationContext.getBean(serviceType);
        Object todo = enumValue(statusType, "TODO");
        Object request = newTask(taskType, statusType, null, "Prepare demo", "Explain lifecycle", todo, LocalDate.of(2099, 5, 1), null);

        Method createMethod = serviceType.getMethod("create", taskType);
        Object created = createMethod.invoke(service, request);

        assertEquals(todo, taskType.getMethod("getStatus").invoke(created), "TaskService.create(Task) should always start tasks with status TODO.");
        assertNull(taskType.getMethod("getCompletedAt").invoke(created), "New tasks should start with completedAt = null.");
    }

    @Test
    void updateStatusToDoneSetsCompletedAtAndReverseTransitionThrowsConflict() throws Exception {
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'.");
        Class<?> serviceType = loadRequiredClass("lt.techin.taskmanager.service.TaskService", "Could not find interface 'lt.techin.taskmanager.service.TaskService'.");

        Object service = applicationContext.getBean(serviceType);
        Object todo = enumValue(statusType, "TODO");
        Object done = enumValue(statusType, "DONE");

        Method createMethod = serviceType.getMethod("create", taskType);
        Method updateStatusMethod = serviceType.getMethod("updateStatus", Long.class, statusType);

        Object created = createMethod.invoke(
                service,
                newTask(taskType, statusType, null, "Finish docs", "Complete the task", todo, LocalDate.of(2099, 6, 1), null)
        );

        Long id = (Long) taskType.getMethod("getId").invoke(created);
        Object completed = updateStatusMethod.invoke(service, id, done);

        assertEquals(done, taskType.getMethod("getStatus").invoke(completed), "Changing status to DONE should persist the DONE state.");
        assertNotNull(taskType.getMethod("getCompletedAt").invoke(completed), "Changing status to DONE should set completedAt.");

        InvocationTargetException reverseThrown = assertThrows(
                InvocationTargetException.class,
                () -> updateStatusMethod.invoke(service, id, todo),
                "Moving from DONE back to TODO should throw IllegalTaskStateException."
        );
        assertEquals(
                "lt.techin.taskmanager.exception.IllegalTaskStateException",
                reverseThrown.getCause().getClass().getName(),
                "Reverse transition should throw IllegalTaskStateException."
        );
    }

    private Class<?> loadRequiredClass(String fqcn, String failureMessage) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException exception) {
            fail(failureMessage);
            return null;
        }
    }

    private Object enumValue(Class<?> enumType, String name) {
        return Enum.valueOf((Class<Enum>) enumType.asSubclass(Enum.class), name);
    }

    private Object newTask(
            Class<?> taskType,
            Class<?> statusType,
            Long id,
            String title,
            String description,
            Object status,
            LocalDate dueDate,
            Object completedAt
    ) {
        try {
            Object task = taskType.getDeclaredConstructor().newInstance();
            taskType.getMethod("setId", Long.class).invoke(task, id);
            taskType.getMethod("setTitle", String.class).invoke(task, title);
            taskType.getMethod("setDescription", String.class).invoke(task, description);
            taskType.getMethod("setStatus", statusType).invoke(task, status);
            taskType.getMethod("setDueDate", LocalDate.class).invoke(task, dueDate);
            taskType.getMethod("setCompletedAt", loadRequiredClass("java.time.LocalDateTime", "LocalDateTime should be used for completedAt.")).invoke(task, completedAt);
            return task;
        } catch (ReflectiveOperationException exception) {
            fail("Could not create or populate Task. Make sure Task has setters for id, title, description, status, dueDate, and completedAt.");
            return null;
        }
    }
}
