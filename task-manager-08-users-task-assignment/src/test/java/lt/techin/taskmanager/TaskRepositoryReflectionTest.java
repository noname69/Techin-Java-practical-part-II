package lt.techin.taskmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    void projectTaskAndUserUseJpaRelationshipAnnotations() throws Exception {
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Could not find class 'lt.techin.taskmanager.model.Project'.");
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> userType = loadRequiredClass("lt.techin.taskmanager.model.User", "Could not find class 'lt.techin.taskmanager.model.User'.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'.");
        Class<?> taskRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.TaskRepository", "Could not find interface 'lt.techin.taskmanager.repository.TaskRepository'.");
        Class<?> projectRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.ProjectRepository", "Could not find interface 'lt.techin.taskmanager.repository.ProjectRepository'.");
        Class<?> userRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.UserRepository", "Could not find interface 'lt.techin.taskmanager.repository.UserRepository'.");

        assertNotNull(projectType.getAnnotation(Entity.class), "Project should be a JPA entity.");
        assertNotNull(taskType.getAnnotation(Entity.class), "Task should remain a JPA entity.");
        assertNotNull(userType.getAnnotation(Entity.class), "User should be a JPA entity.");

        Field userIdField = userType.getDeclaredField("id");
        assertNotNull(userIdField.getAnnotation(Id.class), "User.id should be annotated with @Id.");
        assertNotNull(userIdField.getAnnotation(GeneratedValue.class), "User.id should be annotated with @GeneratedValue.");

        Field taskProjectField = taskType.getDeclaredField("project");
        assertNotNull(taskProjectField.getAnnotation(ManyToOne.class), "Task.project should be annotated with @ManyToOne.");

        Field taskAssigneeField = taskType.getDeclaredField("assignee");
        assertNotNull(taskAssigneeField.getAnnotation(ManyToOne.class), "Task.assignee should be annotated with @ManyToOne.");

        Field projectTasksField = projectType.getDeclaredField("tasks");
        assertNotNull(projectTasksField.getAnnotation(OneToMany.class), "Project.tasks should be annotated with @OneToMany.");

        assertTrue(JpaRepository.class.isAssignableFrom(taskRepositoryType), "TaskRepository should extend JpaRepository.");
        assertTrue(JpaRepository.class.isAssignableFrom(projectRepositoryType), "ProjectRepository should extend JpaRepository.");
        assertTrue(JpaRepository.class.isAssignableFrom(userRepositoryType), "UserRepository should extend JpaRepository.");

        taskRepositoryType.getMethod("findByProjectId", Long.class);
        taskRepositoryType.getMethod("findByProjectIdAndStatus", Long.class, statusType);
        projectRepositoryType.getMethod("existsByName", String.class);
        userRepositoryType.getMethod("existsByEmail", String.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void repositoriesPersistTaskProjectAndOptionalAssignee() throws Exception {
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Could not find class 'lt.techin.taskmanager.model.Project'.");
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> userType = loadRequiredClass("lt.techin.taskmanager.model.User", "Could not find class 'lt.techin.taskmanager.model.User'.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'.");
        Class<?> taskRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.TaskRepository", "Could not find interface 'lt.techin.taskmanager.repository.TaskRepository'.");
        Class<?> projectRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.ProjectRepository", "Could not find interface 'lt.techin.taskmanager.repository.ProjectRepository'.");
        Class<?> userRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.UserRepository", "Could not find interface 'lt.techin.taskmanager.repository.UserRepository'.");

        Object projectRepository = applicationContext.getBean(projectRepositoryType);
        Object taskRepository = applicationContext.getBean(taskRepositoryType);
        Object userRepository = applicationContext.getBean(userRepositoryType);

        Method saveProjectMethod = projectRepositoryType.getMethod("save", Object.class);
        Method saveTaskMethod = taskRepositoryType.getMethod("save", Object.class);
        Method saveUserMethod = userRepositoryType.getMethod("save", Object.class);
        Method findByProjectIdAndStatusMethod = taskRepositoryType.getMethod("findByProjectIdAndStatus", Long.class, statusType);

        Object project = projectType.getDeclaredConstructor().newInstance();
        projectType.getMethod("setName", String.class).invoke(project, "Project A");
        projectType.getMethod("setDescription", String.class).invoke(project, "Repository test project");
        projectType.getMethod("setArchived", boolean.class).invoke(project, false);
        Object savedProject = saveProjectMethod.invoke(projectRepository, project);
        Long projectId = (Long) projectType.getMethod("getId").invoke(savedProject);

        Object user = userType.getDeclaredConstructor().newInstance();
        userType.getMethod("setName", String.class).invoke(user, "Alice");
        userType.getMethod("setEmail", String.class).invoke(user, "alice@example.com");
        Object savedUser = saveUserMethod.invoke(userRepository, user);

        Object todo = Enum.valueOf((Class<Enum>) statusType.asSubclass(Enum.class), "TODO");
        Object done = Enum.valueOf((Class<Enum>) statusType.asSubclass(Enum.class), "DONE");

        Object firstTask = taskType.getDeclaredConstructor().newInstance();
        taskType.getMethod("setTitle", String.class).invoke(firstTask, "One");
        taskType.getMethod("setDescription", String.class).invoke(firstTask, "First task");
        taskType.getMethod("setStatus", statusType).invoke(firstTask, todo);
        taskType.getMethod("setDueDate", LocalDate.class).invoke(firstTask, LocalDate.of(2099, 5, 1));
        taskType.getMethod("setProject", projectType).invoke(firstTask, savedProject);
        taskType.getMethod("setAssignee", userType).invoke(firstTask, savedUser);

        Object secondTask = taskType.getDeclaredConstructor().newInstance();
        taskType.getMethod("setTitle", String.class).invoke(secondTask, "Two");
        taskType.getMethod("setDescription", String.class).invoke(secondTask, "Second task");
        taskType.getMethod("setStatus", statusType).invoke(secondTask, done);
        taskType.getMethod("setDueDate", LocalDate.class).invoke(secondTask, LocalDate.of(2099, 6, 1));
        taskType.getMethod("setProject", projectType).invoke(secondTask, savedProject);

        Object savedFirstTask = saveTaskMethod.invoke(taskRepository, firstTask);
        saveTaskMethod.invoke(taskRepository, secondTask);

        assertNotNull(taskType.getMethod("getId").invoke(savedFirstTask), "Persisting a task should generate an id.");
        assertNotNull(taskType.getMethod("getAssignee").invoke(savedFirstTask), "Persisting a task with assignee should keep the relationship.");

        List<?> doneTasks = (List<?>) findByProjectIdAndStatusMethod.invoke(taskRepository, projectId, done);
        assertEquals(1, doneTasks.size(), "findByProjectIdAndStatus(...) should still filter tasks within the selected project.");
        assertEquals("Two", taskType.getMethod("getTitle").invoke(doneTasks.get(0)), "The filtered task should be the DONE task that belongs to the project.");
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
