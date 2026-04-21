package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskServiceReflectionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void serviceCreatesTaskInsideProjectAndForcesTodoStatus() throws Exception {
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Could not find class 'lt.techin.taskmanager.model.Project'.");
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> projectServiceType = loadRequiredClass("lt.techin.taskmanager.service.ProjectService", "Could not find interface 'lt.techin.taskmanager.service.ProjectService'.");
        Class<?> taskServiceType = loadRequiredClass("lt.techin.taskmanager.service.TaskService", "Could not find interface 'lt.techin.taskmanager.service.TaskService'.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'.");

        Object projectService = getRequiredBean(projectServiceType, "Spring could not create a bean for ProjectService.");
        Object taskService = getRequiredBean(taskServiceType, "Spring could not create a bean for TaskService.");

        Method createProjectMethod = getRequiredMethod(projectServiceType, "create", new Class<?>[]{projectType}, "ProjectService must declare create(Project project).");
        Method createTaskMethod = getRequiredMethod(taskServiceType, "create", new Class<?>[]{Long.class, taskType}, "TaskService must declare create(Long projectId, Task task).");

        Object project = projectType.getDeclaredConstructor().newInstance();
        projectType.getMethod("setName", String.class).invoke(project, "Service Project");
        projectType.getMethod("setDescription", String.class).invoke(project, "Service layer project");
        projectType.getMethod("setArchived", boolean.class).invoke(project, false);
        Object savedProject = invokeRequired(createProjectMethod, projectService, new Object[]{project}, "Calling ProjectService.create(Project) failed.");
        Long projectId = (Long) projectType.getMethod("getId").invoke(savedProject);

        Object task = newTask(taskType, null, "Prepare demo", "Explain relationships", null, LocalDate.of(2099, 5, 1));
        Object created = invokeRequired(createTaskMethod, taskService, new Object[]{projectId, task}, "Calling TaskService.create(Long, Task) failed.");

        assertNotNull(taskType.getMethod("getId").invoke(created), "TaskService.create(Long, Task) should return a saved task with a generated id.");
        assertEquals("TODO", taskType.getMethod("getStatus").invoke(created).toString(), "TaskService.create(Long, Task) must always start new tasks in TODO status.");

        Object projectOnTask = taskType.getMethod("getProject").invoke(created);
        assertNotNull(projectOnTask, "TaskService.create(Long, Task) should attach the task to the resolved project.");
        assertEquals(projectId, projectType.getMethod("getId").invoke(projectOnTask), "The created task should belong to the requested project.");
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
    void serviceRejectsArchivedProjectOnCreate() throws Exception {
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Could not find class 'lt.techin.taskmanager.model.Project'.");
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> projectServiceType = loadRequiredClass("lt.techin.taskmanager.service.ProjectService", "Could not find interface 'lt.techin.taskmanager.service.ProjectService'.");
        Class<?> taskServiceType = loadRequiredClass("lt.techin.taskmanager.service.TaskService", "Could not find interface 'lt.techin.taskmanager.service.TaskService'.");

        Object projectService = getRequiredBean(projectServiceType, "Spring could not create a bean for ProjectService.");
        Object taskService = getRequiredBean(taskServiceType, "Spring could not create a bean for TaskService.");

        Method createProjectMethod = getRequiredMethod(projectServiceType, "create", new Class<?>[]{projectType}, "ProjectService must declare create(Project project).");
        Method updateArchivedMethod = getRequiredMethod(projectServiceType, "updateArchived", new Class<?>[]{Long.class, boolean.class}, "ProjectService must declare updateArchived(Long id, boolean archived).");
        Method createTaskMethod = getRequiredMethod(taskServiceType, "create", new Class<?>[]{Long.class, taskType}, "TaskService must declare create(Long projectId, Task task).");

        Object project = projectType.getDeclaredConstructor().newInstance();
        projectType.getMethod("setName", String.class).invoke(project, "Archived Project");
        projectType.getMethod("setDescription", String.class).invoke(project, "Archive me");
        projectType.getMethod("setArchived", boolean.class).invoke(project, false);
        Object savedProject = invokeRequired(createProjectMethod, projectService, new Object[]{project}, "Calling ProjectService.create(Project) failed.");
        Long projectId = (Long) projectType.getMethod("getId").invoke(savedProject);

        invokeRequired(updateArchivedMethod, projectService, new Object[]{projectId, true}, "Calling ProjectService.updateArchived(Long, boolean) failed.");

        Object task = newTask(taskType, null, "Blocked task", "Should not be created", null, LocalDate.of(2099, 5, 1));

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                () -> invokeRequired(createTaskMethod, taskService, new Object[]{projectId, task}, "Calling TaskService.create(Long, Task) failed unexpectedly."),
                "TaskService.create(Long, Task) should throw ArchivedProjectException when the project is archived."
        );

        assertNotNull(thrown.getCause(), "TaskService.create(Long, Task) should fail because ArchivedProjectException is thrown from inside the service.");
        assertEquals(
                "lt.techin.taskmanager.exception.ArchivedProjectException",
                thrown.getCause().getClass().getName(),
                "TaskService.create(Long, Task) should throw ArchivedProjectException for archived projects."
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
