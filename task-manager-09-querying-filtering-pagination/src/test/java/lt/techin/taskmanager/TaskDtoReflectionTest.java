package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.fail;

class TaskDtoReflectionTest {

    @Test
    void taskPageResponseExistsAndUsesTaskResponseItems() {
        Class<?> taskPageType = loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskPageResponse",
                "Could not find class 'lt.techin.taskmanager.dto.TaskPageResponse'."
        );
        Class<?> taskResponseType = loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskResponse",
                "Could not find class 'lt.techin.taskmanager.dto.TaskResponse'."
        );

        assertHasRecordComponent(taskPageType, "content", "TaskPageResponse should contain content.");
        assertHasRecordComponent(taskPageType, "page", "TaskPageResponse should contain page.");
        assertHasRecordComponent(taskPageType, "size", "TaskPageResponse should contain size.");
        assertHasRecordComponent(taskPageType, "totalElements", "TaskPageResponse should contain totalElements.");
        assertHasRecordComponent(taskPageType, "totalPages", "TaskPageResponse should contain totalPages.");

        Method contentAccessor = getRequiredMethod(taskPageType, "content", new Class<?>[0], "TaskPageResponse should expose a content() accessor.");
        if (!contentAccessor.getGenericReturnType().getTypeName().contains(taskResponseType.getSimpleName())) {
            fail("TaskPageResponse.content should expose TaskResponse items, not entities or raw maps.");
        }
    }

    @Test
    void controllersAndServiceExposePagedSearchMethods() {
        Class<?> taskControllerType = loadRequiredClass(
                "lt.techin.taskmanager.controller.TaskController",
                "Could not find class 'lt.techin.taskmanager.controller.TaskController'."
        );
        Class<?> projectControllerType = loadRequiredClass(
                "lt.techin.taskmanager.controller.ProjectController",
                "Could not find class 'lt.techin.taskmanager.controller.ProjectController'."
        );
        Class<?> taskServiceType = loadRequiredClass(
                "lt.techin.taskmanager.service.TaskService",
                "Could not find interface 'lt.techin.taskmanager.service.TaskService'."
        );
        Class<?> statusType = loadRequiredClass(
                "lt.techin.taskmanager.model.TaskStatus",
                "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'."
        );
        Class<?> pageableType = loadRequiredClass(
                "org.springframework.data.domain.Pageable",
                "Could not load org.springframework.data.domain.Pageable."
        );
        Class<?> pageType = loadRequiredClass(
                "org.springframework.data.domain.Page",
                "Could not load org.springframework.data.domain.Page."
        );
        Class<?> localDateType = loadRequiredClass(
                "java.time.LocalDate",
                "Could not load java.time.LocalDate."
        );

        Method globalSearch = getRequiredMethod(
                taskControllerType,
                "getAll",
                new Class<?>[]{statusType, Long.class, Long.class, localDateType, pageableType},
                "TaskController.getAll(...) should support filters and Pageable."
        );
        Method projectSearch = getRequiredMethod(
                projectControllerType,
                "getTasks",
                new Class<?>[]{Long.class, statusType, Long.class, localDateType, pageableType},
                "ProjectController.getTasks(...) should support project-scoped filters and Pageable."
        );
        Method serviceSearch = getRequiredMethod(
                taskServiceType,
                "search",
                new Class<?>[]{statusType, Long.class, Long.class, localDateType, pageableType},
                "TaskService.search(...) should support global task filtering with Pageable."
        );
        Method serviceProjectSearch = getRequiredMethod(
                taskServiceType,
                "searchByProject",
                new Class<?>[]{Long.class, statusType, Long.class, localDateType, pageableType},
                "TaskService.searchByProject(...) should support project-scoped filtering with Pageable."
        );

        if (!globalSearch.getReturnType().getName().equals("lt.techin.taskmanager.dto.TaskPageResponse")) {
            fail("TaskController.getAll(...) should return TaskPageResponse.");
        }
        if (!projectSearch.getReturnType().getName().equals("lt.techin.taskmanager.dto.TaskPageResponse")) {
            fail("ProjectController.getTasks(...) should return TaskPageResponse.");
        }
        if (!pageType.isAssignableFrom(serviceSearch.getReturnType())) {
            fail("TaskService.search(...) should return Page<Task>.");
        }
        if (!pageType.isAssignableFrom(serviceProjectSearch.getReturnType())) {
            fail("TaskService.searchByProject(...) should return Page<Task>.");
        }
    }

    private Class<?> loadRequiredClass(String fqcn, String failureMessage) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException exception) {
            fail(failureMessage);
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

    private void assertHasRecordComponent(Class<?> type, String componentName, String failureMessage) {
        if (!type.isRecord()) {
            fail("TaskPageResponse should be implemented as a record.");
        }
        for (RecordComponent component : type.getRecordComponents()) {
            if (component.getName().equals(componentName)) {
                return;
            }
        }
        fail(failureMessage);
    }
}
