package lt.techin.taskmanager;

import jakarta.persistence.*;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskRepositoryReflectionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void projectAndTaskUseJpaRelationshipAnnotations() throws Exception {
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Could not find class 'lt.techin.taskmanager.model.Project'.");
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'.");
        Class<?> taskRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.TaskRepository", "Could not find interface 'lt.techin.taskmanager.repository.TaskRepository'.");
        Class<?> projectRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.ProjectRepository", "Could not find interface 'lt.techin.taskmanager.repository.ProjectRepository'.");

        assertNotNull(projectType.getAnnotation(Entity.class), "Project should be a JPA entity.");
        assertNotNull(taskType.getAnnotation(Entity.class), "Task should remain a JPA entity.");

        Field projectIdField = projectType.getDeclaredField("id");
        assertNotNull(projectIdField.getAnnotation(Id.class), "Project.id should be annotated with @Id.");
        assertNotNull(projectIdField.getAnnotation(GeneratedValue.class), "Project.id should be annotated with @GeneratedValue.");

        Field taskProjectField = taskType.getDeclaredField("project");
        assertNotNull(taskProjectField.getAnnotation(ManyToOne.class), "Task.project should be annotated with @ManyToOne.");

        Field projectTasksField = projectType.getDeclaredField("tasks");
        assertNotNull(projectTasksField.getAnnotation(OneToMany.class), "Project.tasks should be annotated with @OneToMany.");

        assertTrue(JpaRepository.class.isAssignableFrom(taskRepositoryType), "TaskRepository should extend JpaRepository.");
        assertTrue(JpaRepository.class.isAssignableFrom(projectRepositoryType), "ProjectRepository should extend JpaRepository.");

        taskRepositoryType.getMethod("findByProjectId", Long.class);
        taskRepositoryType.getMethod("findByProjectIdAndStatus", Long.class, statusType);
        projectRepositoryType.getMethod("existsByName", String.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void repositoriesPersistAndFilterByProjectRelationship() throws Exception {
        Class<?> projectType = loadRequiredClass("lt.techin.taskmanager.model.Project", "Could not find class 'lt.techin.taskmanager.model.Project'.");
        Class<?> taskType = loadRequiredClass("lt.techin.taskmanager.model.Task", "Could not find class 'lt.techin.taskmanager.model.Task'.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'.");
        Class<?> taskRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.TaskRepository", "Could not find interface 'lt.techin.taskmanager.repository.TaskRepository'.");
        Class<?> projectRepositoryType = loadRequiredClass("lt.techin.taskmanager.repository.ProjectRepository", "Could not find interface 'lt.techin.taskmanager.repository.ProjectRepository'.");

        Object projectRepository = applicationContext.getBean(projectRepositoryType);
        Object taskRepository = applicationContext.getBean(taskRepositoryType);

        Method saveProjectMethod = projectRepositoryType.getMethod("save", Object.class);
        Method saveTaskMethod = taskRepositoryType.getMethod("save", Object.class);
        Method findByProjectIdAndStatusMethod = taskRepositoryType.getMethod("findByProjectIdAndStatus", Long.class, statusType);

        Object project = projectType.getDeclaredConstructor().newInstance();
        projectType.getMethod("setName", String.class).invoke(project, "Project A");
        projectType.getMethod("setDescription", String.class).invoke(project, "Repository test project");
        projectType.getMethod("setArchived", boolean.class).invoke(project, false);
        Object savedProject = saveProjectMethod.invoke(projectRepository, project);
        Long projectId = (Long) projectType.getMethod("getId").invoke(savedProject);

        Object todo = Enum.valueOf((Class<Enum>) statusType.asSubclass(Enum.class), "TODO");
        Object done = Enum.valueOf((Class<Enum>) statusType.asSubclass(Enum.class), "DONE");

        Object firstTask = taskType.getDeclaredConstructor().newInstance();
        taskType.getMethod("setTitle", String.class).invoke(firstTask, "One");
        taskType.getMethod("setDescription", String.class).invoke(firstTask, "First task");
        taskType.getMethod("setStatus", statusType).invoke(firstTask, todo);
        taskType.getMethod("setDueDate", LocalDate.class).invoke(firstTask, LocalDate.of(2099, 5, 1));
        taskType.getMethod("setProject", projectType).invoke(firstTask, savedProject);

        Object secondTask = taskType.getDeclaredConstructor().newInstance();
        taskType.getMethod("setTitle", String.class).invoke(secondTask, "Two");
        taskType.getMethod("setDescription", String.class).invoke(secondTask, "Second task");
        taskType.getMethod("setStatus", statusType).invoke(secondTask, done);
        taskType.getMethod("setDueDate", LocalDate.class).invoke(secondTask, LocalDate.of(2099, 6, 1));
        taskType.getMethod("setProject", projectType).invoke(secondTask, savedProject);

        Object savedFirstTask = saveTaskMethod.invoke(taskRepository, firstTask);
        saveTaskMethod.invoke(taskRepository, secondTask);

        assertNotNull(taskType.getMethod("getId").invoke(savedFirstTask), "Persisting a task should generate an id.");
        List<?> doneTasks = (List<?>) findByProjectIdAndStatusMethod.invoke(taskRepository, projectId, done);
        assertEquals(1, doneTasks.size(), "findByProjectIdAndStatus(...) should filter tasks within the selected project.");
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
