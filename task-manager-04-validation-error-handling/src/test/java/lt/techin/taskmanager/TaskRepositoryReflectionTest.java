package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskRepositoryReflectionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void repositoryStoresTasksAssignsIdsAndFiltersByDone() throws Exception {
        Class<?> taskType = loadRequiredClass(
                "lt.techin.taskmanager.model.Task",
                "Could not find class 'lt.techin.taskmanager.model.Task'. Copy or create your Task model in the model package."
        );
        Class<?> repositoryType = loadRequiredClass(
                "lt.techin.taskmanager.repository.TaskRepository",
                "Could not find interface 'lt.techin.taskmanager.repository.TaskRepository'. Create the repository contract in the repository package."
        );

        Object repository = getRequiredBean(
                repositoryType,
                "Spring could not create a bean for TaskRepository. Check your InMemoryTaskRepository class, @Repository annotation, and constructor wiring."
        );

        Method saveMethod = getRequiredMethod(
                repositoryType,
                "save",
                new Class<?>[]{taskType},
                "TaskRepository must declare method 'save(Task task)'."
        );
        Method findAllMethod = getRequiredMethod(
                repositoryType,
                "findAll",
                new Class<?>[]{},
                "TaskRepository must declare method 'findAll()'."
        );
        Method findByDoneMethod = getRequiredMethod(
                repositoryType,
                "findByDone",
                new Class<?>[]{boolean.class},
                "TaskRepository must declare method 'findByDone(boolean done)'."
        );

        Object first = newTask(taskType, null, "One", "First task", false, LocalDate.of(2099, 5, 1));
        Object second = newTask(taskType, null, "Two", "Second task", true, LocalDate.of(2099, 6, 1));

        Object savedFirst = invokeRequired(saveMethod, repository, new Object[]{first}, "Calling TaskRepository.save(Task) failed for the first task.");
        Object savedSecond = invokeRequired(saveMethod, repository, new Object[]{second}, "Calling TaskRepository.save(Task) failed for the second task.");

        assertEquals(
                1L,
                invokeRequiredGetter(taskType, savedFirst, "getId", "Task must have method 'getId()'."),
                "The first saved task should receive id=1. The in-memory repository should start ID generation from 1 in a fresh application context."
        );
        assertEquals(
                2L,
                invokeRequiredGetter(taskType, savedSecond, "getId", "Task must have method 'getId()'."),
                "The second saved task should receive id=2. The repository should increment ids for each newly saved task."
        );

        List<?> all = (List<?>) invokeRequired(findAllMethod, repository, new Object[]{}, "Calling TaskRepository.findAll() failed.");
        List<?> doneTasks = (List<?>) invokeRequired(findByDoneMethod, repository, new Object[]{true}, "Calling TaskRepository.findByDone(boolean) failed.");

        assertEquals(2, all.size(), "TaskRepository.findAll() should return both saved tasks.");
        assertEquals(1, doneTasks.size(), "TaskRepository.findByDone(true) should return only tasks whose done field is true.");
        assertEquals(
                "Two",
                invokeRequiredGetter(taskType, doneTasks.get(0), "getTitle", "Task must have method 'getTitle()'."),
                "TaskRepository.findByDone(true) should return the task whose title is 'Two' in this test scenario."
        );
        assertEquals(
                LocalDate.of(2099, 6, 1),
                invokeRequiredGetter(taskType, doneTasks.get(0), "getDueDate", "Task must have method 'getDueDate()'."),
                "TaskRepository should keep the dueDate field when storing tasks in memory."
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

    private Object getRequiredBean(Class<?> type, String failureMessage) {
        try {
            return applicationContext.getBean(type);
        } catch (Exception exception) {
            fail(failureMessage + " Original error: " + exception.getClass().getSimpleName());
            return null;
        }
    }

    private Method getRequiredMethod(Class<?> type, String name, Class<?>[] parameterTypes, String failureMessage) {
        try {
            return type.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException exception) {
            fail(failureMessage);
            return null;
        }
    }

    private Object invokeRequired(Method method, Object target, Object[] args, String failureMessage) {
        try {
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            fail(failureMessage);
            return null;
        }
    }

    private Object invokeRequiredGetter(Class<?> taskType, Object target, String getterName, String failureMessage) {
        try {
            return taskType.getMethod(getterName).invoke(target);
        } catch (ReflectiveOperationException exception) {
            fail(failureMessage);
            return null;
        }
    }

    private Object newTask(Class<?> taskType, Long id, String title, String description, boolean done, LocalDate dueDate) {
        try {
            Object task = taskType.getDeclaredConstructor().newInstance();
            taskType.getMethod("setId", Long.class).invoke(task, id);
            taskType.getMethod("setTitle", String.class).invoke(task, title);
            taskType.getMethod("setDescription", String.class).invoke(task, description);
            taskType.getMethod("setDone", boolean.class).invoke(task, done);
            taskType.getMethod("setDueDate", LocalDate.class).invoke(task, dueDate);
            return task;
        } catch (ReflectiveOperationException exception) {
            fail("Could not create or populate Task. Make sure Task has a no-args constructor and setters for id, title, description, done, and dueDate.");
            return null;
        }
    }
}
