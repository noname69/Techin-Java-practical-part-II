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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskServiceReflectionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void serviceCreatesTaskInsideProjectAndAttachesOptionalAssignee() throws Exception {
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Could not find class 'lt.techin.taskmanager.model.Project'.");
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> userType = loadRequiredClass("lt.techin.taskmanager.model.User", "Could not find class 'lt.techin.taskmanager.model.User'.");
        Class<?> projectServiceType = loadRequiredClass("lt.techin.taskmanager.service.ProjectService", "Could not find interface 'lt.techin.taskmanager.service.ProjectService'.");
        Class<?> taskServiceType = loadRequiredClass("lt.techin.taskmanager.service.TaskService", "Could not find interface 'lt.techin.taskmanager.service.TaskService'.");
        Class<?> userServiceType = loadRequiredClass("lt.techin.taskmanager.service.UserService", "Could not find interface 'lt.techin.taskmanager.service.UserService'.");

        Object projectService = getRequiredBean(projectServiceType, "Spring could not create a bean for ProjectService.");
        Object taskService = getRequiredBean(taskServiceType, "Spring could not create a bean for TaskService.");
        Object userService = getRequiredBean(userServiceType, "Spring could not create a bean for UserService.");

        Method createProjectMethod = getRequiredMethod(projectServiceType, "create", new Class<?>[]{projectType}, "ProjectService must declare create(Project project).");
        Method createTaskMethod = getRequiredMethod(taskServiceType, "create", new Class<?>[]{Long.class, taskType, Long.class}, "TaskService must declare create(Long projectId, Task task, Long assigneeId).");
        Method createUserMethod = getRequiredMethod(userServiceType, "create", new Class<?>[]{userType}, "UserService must declare create(User user).");

        Object project = projectType.getDeclaredConstructor().newInstance();
        projectType.getMethod("setName", String.class).invoke(project, "Service Project");
        projectType.getMethod("setDescription", String.class).invoke(project, "Service layer project");
        projectType.getMethod("setArchived", boolean.class).invoke(project, false);
        Object savedProject = invokeRequired(createProjectMethod, projectService, new Object[]{project}, "Calling ProjectService.create(Project) failed.");
        Long projectId = (Long) projectType.getMethod("getId").invoke(savedProject);

        Object user = userType.getDeclaredConstructor().newInstance();
        userType.getMethod("setName", String.class).invoke(user, "Alice");
        userType.getMethod("setEmail", String.class).invoke(user, "alice@example.com");
        Object savedUser = invokeRequired(createUserMethod, userService, new Object[]{user}, "Calling UserService.create(User) failed.");
        Long assigneeId = (Long) userType.getMethod("getId").invoke(savedUser);

        Object task = newTask(taskType, null, "Prepare demo", "Explain assignment", null, LocalDate.of(2099, 5, 1));
        Object created = invokeRequired(createTaskMethod, taskService, new Object[]{projectId, task, assigneeId}, "Calling TaskService.create(Long, Task, Long) failed.");

        assertNotNull(taskType.getMethod("getId").invoke(created), "TaskService.create(...) should return a saved task with a generated id.");
        assertEquals("TODO", taskType.getMethod("getStatus").invoke(created).toString(), "TaskService.create(...) must always start new tasks in TODO status.");
        assertNotNull(taskType.getMethod("getProject").invoke(created), "TaskService.create(...) should attach the task to the resolved project.");
        assertNotNull(taskType.getMethod("getAssignee").invoke(created), "TaskService.create(...) should attach the task to the resolved assignee when assigneeId is provided.");
    }

    @Test
    void serviceThrowsTaskNotFoundExceptionForMissingIds() throws Exception {
        Class<?> serviceType = loadRequiredClass("lt.techin.taskmanager.service.TaskService", "Could not find interface 'lt.techin.taskmanager.service.TaskService'.");
        Object service = getRequiredBean(serviceType, "Spring could not create a bean for TaskService.");
        Method getByIdMethod = getRequiredMethod(serviceType, "getById", new Class<?>[]{Long.class}, "TaskService must declare getById(Long id).");

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
    void serviceRejectsMissingAssigneeAndCompletedTaskReassignment() throws Exception {
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Could not find class 'lt.techin.taskmanager.model.Project'.");
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> userType = loadRequiredClass("lt.techin.taskmanager.model.User", "Could not find class 'lt.techin.taskmanager.model.User'.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'.");
        Class<?> projectServiceType = loadRequiredClass("lt.techin.taskmanager.service.ProjectService", "Could not find interface 'lt.techin.taskmanager.service.ProjectService'.");
        Class<?> taskServiceType = loadRequiredClass("lt.techin.taskmanager.service.TaskService", "Could not find interface 'lt.techin.taskmanager.service.TaskService'.");
        Class<?> userServiceType = loadRequiredClass("lt.techin.taskmanager.service.UserService", "Could not find interface 'lt.techin.taskmanager.service.UserService'.");

        Object projectService = getRequiredBean(projectServiceType, "Spring could not create a bean for ProjectService.");
        Object taskService = getRequiredBean(taskServiceType, "Spring could not create a bean for TaskService.");
        Object userService = getRequiredBean(userServiceType, "Spring could not create a bean for UserService.");

        Method createProjectMethod = getRequiredMethod(projectServiceType, "create", new Class<?>[]{projectType}, "ProjectService must declare create(Project project).");
        Method createTaskMethod = getRequiredMethod(taskServiceType, "create", new Class<?>[]{Long.class, taskType, Long.class}, "TaskService must declare create(Long projectId, Task task, Long assigneeId).");
        Method updateTaskMethod = getRequiredMethod(taskServiceType, "update", new Class<?>[]{Long.class, taskType, Long.class, Long.class}, "TaskService must declare update(Long id, Task task, Long projectId, Long assigneeId).");
        Method updateStatusMethod = getRequiredMethod(taskServiceType, "updateStatus", new Class<?>[]{Long.class, statusType}, "TaskService must declare updateStatus(Long id, TaskStatus value).");
        Method createUserMethod = getRequiredMethod(userServiceType, "create", new Class<?>[]{userType}, "UserService must declare create(User user).");

        Object project = projectType.getDeclaredConstructor().newInstance();
        projectType.getMethod("setName", String.class).invoke(project, "Assignment Project");
        projectType.getMethod("setDescription", String.class).invoke(project, "Service assignment project");
        projectType.getMethod("setArchived", boolean.class).invoke(project, false);
        Object savedProject = invokeRequired(createProjectMethod, projectService, new Object[]{project}, "Calling ProjectService.create(Project) failed.");
        Long projectId = (Long) projectType.getMethod("getId").invoke(savedProject);

        Object userOne = userType.getDeclaredConstructor().newInstance();
        userType.getMethod("setName", String.class).invoke(userOne, "Alice");
        userType.getMethod("setEmail", String.class).invoke(userOne, "alice@example.com");
        Object savedUserOne = invokeRequired(createUserMethod, userService, new Object[]{userOne}, "Calling UserService.create(User) failed.");
        Long assigneeOneId = (Long) userType.getMethod("getId").invoke(savedUserOne);

        Object userTwo = userType.getDeclaredConstructor().newInstance();
        userType.getMethod("setName", String.class).invoke(userTwo, "Bob");
        userType.getMethod("setEmail", String.class).invoke(userTwo, "bob@example.com");
        Object savedUserTwo = invokeRequired(createUserMethod, userService, new Object[]{userTwo}, "Calling UserService.create(User) failed.");
        Long assigneeTwoId = (Long) userType.getMethod("getId").invoke(savedUserTwo);

        Object task = newTask(taskType, null, "Prepare demo", "Explain assignment", null, LocalDate.of(2099, 5, 1));

        InvocationTargetException missingUserThrown = assertThrows(
                InvocationTargetException.class,
                () -> invokeRequired(createTaskMethod, taskService, new Object[]{projectId, task, 99L}, "Calling TaskService.create(...) failed unexpectedly."),
                "TaskService.create(...) should throw UserNotFoundException when assigneeId does not exist."
        );

        assertEquals(
                "lt.techin.taskmanager.exception.UserNotFoundException",
                missingUserThrown.getCause().getClass().getName(),
                "TaskService.create(...) should throw UserNotFoundException for missing assignee ids."
        );

        Object created = invokeRequired(createTaskMethod, taskService, new Object[]{projectId, task, assigneeOneId}, "Calling TaskService.create(...) failed.");
        Long taskId = (Long) taskType.getMethod("getId").invoke(created);

        Object doneStatus = Enum.valueOf((Class<Enum>) statusType.asSubclass(Enum.class), "DONE");
        invokeRequired(updateStatusMethod, taskService, new Object[]{taskId, doneStatus}, "Calling TaskService.updateStatus(Long, TaskStatus) failed.");

        Object updateTask = newTask(taskType, taskId, "Prepare demo", "Try to reassign", doneStatus, LocalDate.of(2099, 5, 1));

        InvocationTargetException reassignmentThrown = assertThrows(
                InvocationTargetException.class,
                () -> invokeRequired(updateTaskMethod, taskService, new Object[]{taskId, updateTask, projectId, assigneeTwoId}, "Calling TaskService.update(...) failed unexpectedly."),
                "TaskService.update(...) should throw IllegalTaskStateException when a completed task is reassigned."
        );

        assertEquals(
                "lt.techin.taskmanager.exception.IllegalTaskStateException",
                reassignmentThrown.getCause().getClass().getName(),
                "TaskService.update(...) should throw IllegalTaskStateException when a completed task is reassigned."
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

    private Object newTask(Class<?> taskType, Long id, String title, String description, Object status, LocalDate dueDate) {
        try {
            Object task = taskType.getDeclaredConstructor().newInstance();
            taskType.getMethod("setId", Long.class).invoke(task, id);
            taskType.getMethod("setTitle", String.class).invoke(task, title);
            taskType.getMethod("setDescription", String.class).invoke(task, description);
            if (status != null) {
                Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find TaskStatus.");
                taskType.getMethod("setStatus", statusType).invoke(task, status);
            }
            taskType.getMethod("setDueDate", LocalDate.class).invoke(task, dueDate);
            return task;
        } catch (ReflectiveOperationException exception) {
            fail("Could not create or populate Task. Make sure Task has a no-args constructor and setters for id, title, description, status, and dueDate.");
            return null;
        }
    }
}
