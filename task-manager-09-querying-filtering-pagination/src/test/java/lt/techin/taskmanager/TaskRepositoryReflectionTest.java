package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskRepositoryReflectionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void taskRepositorySupportsSpecificationBasedPaging() throws Exception {
        Class<?> taskRepositoryType = loadRequiredClass(
                "lt.techin.taskmanager.repository.TaskRepository",
                "Could not find interface 'lt.techin.taskmanager.repository.TaskRepository'."
        );
        Class<?> taskSpecificationsType = loadRequiredClass(
                "lt.techin.taskmanager.repository.TaskSpecifications",
                "Could not find class 'lt.techin.taskmanager.repository.TaskSpecifications'."
        );
        Class<?> statusType = loadRequiredClass(
                "lt.techin.taskmanager.model.TaskStatus",
                "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'."
        );

        assertTrue(
                JpaSpecificationExecutor.class.isAssignableFrom(taskRepositoryType),
                "TaskRepository should extend JpaSpecificationExecutor so task filters can be combined without many repository methods."
        );

        taskSpecificationsType.getMethod("withFilters", Long.class, statusType, Long.class, LocalDate.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void specificationSearchFiltersAndPagesTasks() throws Exception {
        Class<?> projectRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.ProjectRepository", "Missing ProjectRepository.");
        Class<?> taskRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.TaskRepository", "Missing TaskRepository.");
        Class<?> userRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.UserRepository", "Missing UserRepository.");
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Missing Project.");
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Missing Task.");
        Class<?> userType = loadRequiredClass("lt.techin.taskmanager.model.User", "Missing User.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Missing TaskStatus.");
        Class<?> taskSpecificationsType = loadRequiredClass("lt.techin.taskmanager.repository.TaskSpecifications", "Missing TaskSpecifications.");

        Object projectRepository = applicationContext.getBean(projectRepositoryType);
        Object taskRepository = applicationContext.getBean(taskRepositoryType);
        Object userRepository = applicationContext.getBean(userRepositoryType);

        Method saveProject = projectRepositoryType.getMethod("save", Object.class);
        Method saveTask = taskRepositoryType.getMethod("save", Object.class);
        Method saveUser = userRepositoryType.getMethod("save", Object.class);
        Method withFilters = taskSpecificationsType.getMethod("withFilters", Long.class, statusType, Long.class, LocalDate.class);
        Method findAll = taskRepositoryType.getMethod("findAll", Specification.class, Pageable.class);

        Object project = projectType.getDeclaredConstructor().newInstance();
        projectType.getMethod("setName", String.class).invoke(project, "Query Project");
        projectType.getMethod("setDescription", String.class).invoke(project, "Project for repository search");
        projectType.getMethod("setArchived", boolean.class).invoke(project, false);
        Object savedProject = saveProject.invoke(projectRepository, project);
        Long projectId = (Long) projectType.getMethod("getId").invoke(savedProject);

        Object user = userType.getDeclaredConstructor().newInstance();
        userType.getMethod("setName", String.class).invoke(user, "Alice");
        userType.getMethod("setEmail", String.class).invoke(user, "alice@example.com");
        Object savedUser = saveUser.invoke(userRepository, user);
        Long userId = (Long) userType.getMethod("getId").invoke(savedUser);

        Object todo = Enum.valueOf((Class<Enum>) statusType.asSubclass(Enum.class), "TODO");
        Object done = Enum.valueOf((Class<Enum>) statusType.asSubclass(Enum.class), "DONE");

        createTask(taskType, saveTask, taskRepository, "One", LocalDate.of(2099, 5, 1), todo, savedProject, savedUser);
        createTask(taskType, saveTask, taskRepository, "Two", LocalDate.of(2099, 5, 2), done, savedProject, null);
        createTask(taskType, saveTask, taskRepository, "Three", LocalDate.of(2099, 5, 3), todo, savedProject, savedUser);

        Object specification = withFilters.invoke(null, projectId, todo, userId, LocalDate.of(2099, 5, 10));
        Page<?> page = (Page<?>) findAll.invoke(taskRepository, specification, PageRequest.of(0, 1));

        assertEquals(2L, page.getTotalElements(), "Combined filters should match only TODO tasks for the selected project, assignee, and due date range.");
        assertEquals(1, page.getSize(), "Paging should respect the requested page size.");
        assertEquals(2, page.getTotalPages(), "Paging should split the matching result into multiple pages when needed.");
    }

    private void createTask(Class<?> taskType, Method saveTask, Object taskRepository, String title, LocalDate dueDate, Object status, Object project, Object assignee) throws Exception {
        Object task = taskType.getDeclaredConstructor().newInstance();
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Missing Project.");
        Class<?> userType = loadRequiredClass("lt.techin.taskmanager.model.User", "Missing User.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Missing TaskStatus.");
        taskType.getMethod("setTitle", String.class).invoke(task, title);
        taskType.getMethod("setDescription", String.class).invoke(task, title + " description");
        taskType.getMethod("setStatus", statusType).invoke(task, status);
        taskType.getMethod("setDueDate", LocalDate.class).invoke(task, dueDate);
        taskType.getMethod("setProject", projectType).invoke(task, project);
        if (assignee != null) {
            taskType.getMethod("setAssignee", userType).invoke(task, assignee);
        }
        saveTask.invoke(taskRepository, task);
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
