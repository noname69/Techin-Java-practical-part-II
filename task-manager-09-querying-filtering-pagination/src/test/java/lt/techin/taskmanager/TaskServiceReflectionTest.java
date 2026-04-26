package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskServiceReflectionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @SuppressWarnings("unchecked")
    void serviceSupportsGlobalAndProjectScopedPagedQueries() throws Exception {
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Missing Project.");
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Missing Task.");
        Class<?> userType = loadRequiredClass("lt.techin.taskmanager.model.User", "Missing User.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Missing TaskStatus.");
        Class<?> projectServiceType = loadRequiredClass("lt.techin.taskmanager.service.ProjectService", "Missing ProjectService.");
        Class<?> taskServiceType = loadRequiredClass("lt.techin.taskmanager.service.TaskService", "Missing TaskService.");
        Class<?> userServiceType = loadRequiredClass("lt.techin.taskmanager.service.UserService", "Missing UserService.");
        Class<?> pageableType = loadRequiredClass("org.springframework.data.domain.Pageable", "Missing Pageable.");

        Object projectService = applicationContext.getBean(projectServiceType);
        Object taskService = applicationContext.getBean(taskServiceType);
        Object userService = applicationContext.getBean(userServiceType);

        Method createProject = projectServiceType.getMethod("create", projectType);
        Method createTask = taskServiceType.getMethod("create", Long.class, taskType, Long.class);
        Method search = taskServiceType.getMethod("search", statusType, Long.class, Long.class, LocalDate.class, pageableType);
        Method searchByProject = taskServiceType.getMethod("searchByProject", Long.class, statusType, Long.class, LocalDate.class, pageableType);
        Method createUser = userServiceType.getMethod("create", userType);

        Object project = projectType.getDeclaredConstructor().newInstance();
        projectType.getMethod("setName", String.class).invoke(project, "Paged Project");
        projectType.getMethod("setDescription", String.class).invoke(project, "Service search project");
        projectType.getMethod("setArchived", boolean.class).invoke(project, false);
        Object savedProject = createProject.invoke(projectService, project);
        Long projectId = (Long) projectType.getMethod("getId").invoke(savedProject);

        Object user = userType.getDeclaredConstructor().newInstance();
        userType.getMethod("setName", String.class).invoke(user, "Alice");
        userType.getMethod("setEmail", String.class).invoke(user, "alice@example.com");
        Object savedUser = createUser.invoke(userService, user);
        Long userId = (Long) userType.getMethod("getId").invoke(savedUser);

        createTask.invoke(taskService, projectId, newTask(taskType, "One", LocalDate.of(2099, 5, 1)), userId);
        createTask.invoke(taskService, projectId, newTask(taskType, "Two", LocalDate.of(2099, 5, 2)), userId);
        createTask.invoke(taskService, projectId, newTask(taskType, "Three", LocalDate.of(2099, 5, 20)), null);

        Object todo = Enum.valueOf((Class<Enum>) statusType.asSubclass(Enum.class), "TODO");

        Page<?> globalPage = (Page<?>) search.invoke(taskService, todo, null, userId, LocalDate.of(2099, 5, 10), PageRequest.of(0, 1));
        Page<?> projectPage = (Page<?>) searchByProject.invoke(taskService, projectId, todo, userId, LocalDate.of(2099, 5, 10), PageRequest.of(0, 5));

        assertEquals(2L, globalPage.getTotalElements(), "TaskService.search(...) should combine optional filters and paging.");
        assertEquals(1, globalPage.getSize(), "TaskService.search(...) should respect the requested page size.");
        assertEquals(2L, projectPage.getTotalElements(), "TaskService.searchByProject(...) should stay project-scoped while applying additional filters.");
    }

    private Object newTask(Class<?> taskType, String title, LocalDate dueDate) {
        try {
            Object task = taskType.getDeclaredConstructor().newInstance();
            taskType.getMethod("setTitle", String.class).invoke(task, title);
            taskType.getMethod("setDescription", String.class).invoke(task, title + " description");
            taskType.getMethod("setDueDate", LocalDate.class).invoke(task, dueDate);
            return task;
        } catch (ReflectiveOperationException exception) {
            fail("Could not create Task. Make sure Task has a no-args constructor and setters for title, description, and dueDate.");
            return null;
        }
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
