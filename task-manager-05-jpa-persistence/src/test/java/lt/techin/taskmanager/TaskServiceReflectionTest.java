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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskServiceReflectionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void serviceCreateAlwaysForcesDoneFalse() throws Exception {
        Class<?> taskType = loadRequiredClass(
                "lt.techin.taskmanager.model.Task",
                "Could not find class 'lt.techin.taskmanager.model.Task'. Copy or create your Task model in the model package."
        );
        Class<?> serviceType = loadRequiredClass(
                "lt.techin.taskmanager.service.TaskService",
                "Could not find interface 'lt.techin.taskmanager.service.TaskService'. Create the service contract in the service package."
        );

        Object service = getRequiredBean(
                serviceType,
                "Spring could not create a bean for TaskService. Check your service interface, implementation, @Service annotation, and constructor injection."
        );
        Object request = newTask(taskType, null, "Prepare demo", "Explain layers", true, LocalDate.of(2099, 5, 1));

        Method createMethod = getRequiredMethod(
                serviceType,
                "create",
                new Class<?>[]{taskType},
                "TaskService must declare method 'create(Task task)'."
        );

        Object created = invokeRequired(
                createMethod,
                service,
                new Object[]{request},
                "Calling TaskService.create(Task) failed. Check your service implementation and bean wiring."
        );

        Object createdId = invokeRequiredGetter(
                taskType,
                created,
                "getId",
                "Task must have method 'getId()'."
        );
        boolean createdDone = (Boolean) invokeRequiredGetter(
                taskType,
                created,
                "isDone",
                "Task must have method 'isDone()'."
        );
        Object createdTitle = invokeRequiredGetter(
                taskType,
                created,
                "getTitle",
                "Task must have method 'getTitle()'."
        );
        Object createdDueDate = invokeRequiredGetter(
                taskType,
                created,
                "getDueDate",
                "Task must have method 'getDueDate()'."
        );

        assertNotNull(createdId, "TaskService.create(Task) should return a saved task with a generated id.");
        assertFalse(createdDone, "TaskService.create(Task) must always force done=false, even if the incoming task has done=true.");
        assertEquals("Prepare demo", createdTitle, "TaskService.create(Task) should preserve the incoming title when creating a task.");
        assertEquals(LocalDate.of(2099, 5, 1), createdDueDate, "TaskService.create(Task) should preserve the incoming dueDate when creating a task.");
    }

    @Test
    void serviceThrowsTaskNotFoundExceptionForMissingIds() throws Exception {
        Class<?> serviceType = loadRequiredClass(
                "lt.techin.taskmanager.service.TaskService",
                "Could not find interface 'lt.techin.taskmanager.service.TaskService'. Create the service contract in the service package."
        );
        Object service = getRequiredBean(
                serviceType,
                "Spring could not create a bean for TaskService. Check your service implementation, @Service annotation, and constructor injection."
        );

        Method getByIdMethod = getRequiredMethod(
                serviceType,
                "getById",
                new Class<?>[]{Long.class},
                "TaskService must declare method 'getById(Long id)'."
        );

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                () -> invokeRequired(getByIdMethod, service, new Object[]{99L}, "Calling TaskService.getById(Long) failed unexpectedly."),
                "TaskService.getById(Long) should throw TaskNotFoundException when the task does not exist."
        );

        assertNotNull(thrown.getCause(), "TaskService.getById(Long) should fail because a TaskNotFoundException is thrown from inside the service.");
        assertEquals(
                "lt.techin.taskmanager.exception.TaskNotFoundException",
                thrown.getCause().getClass().getName(),
                "TaskService.getById(Long) should throw TaskNotFoundException for missing ids."
        );
    }

    @Test
    void serviceThrowsIllegalTaskStateExceptionForRepeatedDoneTruePatch() throws Exception {
        Class<?> taskType = loadRequiredClass(
                "lt.techin.taskmanager.model.Task",
                "Could not find class 'lt.techin.taskmanager.model.Task'. Copy or create your Task model in the model package."
        );
        Class<?> serviceType = loadRequiredClass(
                "lt.techin.taskmanager.service.TaskService",
                "Could not find interface 'lt.techin.taskmanager.service.TaskService'. Create the service contract in the service package."
        );

        Object service = getRequiredBean(
                serviceType,
                "Spring could not create a bean for TaskService. Check your service implementation, @Service annotation, and constructor injection."
        );

        Method createMethod = getRequiredMethod(
                serviceType,
                "create",
                new Class<?>[]{taskType},
                "TaskService must declare method 'create(Task task)'."
        );
        Method updateDoneMethod = getRequiredMethod(
                serviceType,
                "updateDone",
                new Class<?>[]{Long.class, boolean.class},
                "TaskService must declare method 'updateDone(Long id, boolean value)'."
        );

        Object created = invokeRequired(
                createMethod,
                service,
                new Object[]{newTask(taskType, null, "Finish docs", "Close the task", false, LocalDate.of(2099, 7, 1))},
                "Calling TaskService.create(Task) failed."
        );
        Long id = (Long) invokeRequiredGetter(
                taskType,
                created,
                "getId",
                "Task must have method 'getId()'."
        );

        invokeRequired(
                updateDoneMethod,
                service,
                new Object[]{id, true},
                "Calling TaskService.updateDone(Long, boolean) failed on the first done=true update."
        );

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                () -> invokeRequired(
                        updateDoneMethod,
                        service,
                        new Object[]{id, true},
                        "Calling TaskService.updateDone(Long, boolean) failed unexpectedly."
                ),
                "TaskService.updateDone(Long, boolean) should throw IllegalTaskStateException when done=true is requested for a task that is already done."
        );

        assertNotNull(thrown.getCause(), "TaskService.updateDone(Long, boolean) should fail because IllegalTaskStateException is thrown from inside the service.");
        assertEquals(
                "lt.techin.taskmanager.exception.IllegalTaskStateException",
                thrown.getCause().getClass().getName(),
                "TaskService.updateDone(Long, boolean) should throw IllegalTaskStateException for repeated done=true requests."
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

    private Object invokeRequired(Method method, Object target, Object[] args, String failureMessage) throws InvocationTargetException {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException exception) {
            fail(failureMessage + " The method exists but could not be accessed.");
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
