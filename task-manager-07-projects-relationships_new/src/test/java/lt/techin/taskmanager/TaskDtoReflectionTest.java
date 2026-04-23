package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class TaskDtoReflectionTest {

    @Test
    void dtoTypesExistAndRelationshipFieldsAreExposedThroughDtos() {
        Class<?> createProjectType = loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateProjectRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateProjectRequest'."
        );
        Class<?> patchProjectType = loadRequiredClass(
                "lt.techin.taskmanager.dto.PatchProjectRequest",
                "Could not find class 'lt.techin.taskmanager.dto.PatchProjectRequest'."
        );
        Class<?> projectResponseType = loadRequiredClass(
                "lt.techin.taskmanager.dto.ProjectResponse",
                "Could not find class 'lt.techin.taskmanager.dto.ProjectResponse'."
        );
        Class<?> projectSummaryType = loadRequiredClass(
                "lt.techin.taskmanager.dto.ProjectSummaryResponse",
                "Could not find class 'lt.techin.taskmanager.dto.ProjectSummaryResponse'."
        );
        Class<?> createTaskType = loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateTaskRequest'."
        );
        Class<?> updateTaskType = loadRequiredClass(
                "lt.techin.taskmanager.dto.UpdateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.UpdateTaskRequest'."
        );
        Class<?> taskResponseType = loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskResponse",
                "Could not find class 'lt.techin.taskmanager.dto.TaskResponse'."
        );

        assertHasMember(updateTaskType, "projectId", "UpdateTaskRequest should contain projectId so a task can be moved to another project.");
        assertHasMember(taskResponseType, "project", "TaskResponse should contain project summary data.");
        assertHasMember(projectSummaryType, "name", "ProjectSummaryResponse should contain name.");
        assertHasMember(patchProjectType, "archived", "PatchProjectRequest should contain archived.");

        assertValidationAnnotation(createProjectType, "name", "jakarta.validation.constraints.NotBlank", "CreateProjectRequest.name should have @NotBlank.");
        assertValidationAnnotation(createProjectType, "name", "jakarta.validation.constraints.Size", "CreateProjectRequest.name should have @Size.");
        assertValidationAnnotation(createTaskType, "dueDate", "jakarta.validation.constraints.FutureOrPresent", "CreateTaskRequest.dueDate should keep @FutureOrPresent.");

        if (hasMember(createTaskType, "projectId")) {
            fail("CreateTaskRequest should not contain projectId in this task, because project comes from the path variable.");
        }
        if (hasMember(projectResponseType, "tasks")) {
            fail("ProjectResponse should not expose the full tasks collection. Use DTOs to avoid recursion and oversized JSON.");
        }
    }

    @Test
    void controllersUseProjectScopedTaskCreationAndDtoBoundaries() {
        Class<?> projectControllerType = loadRequiredClass(
                "lt.techin.taskmanager.controller.ProjectController",
                "Could not find class 'lt.techin.taskmanager.controller.ProjectController'."
        );
        Class<?> taskControllerType = loadRequiredClass(
                "lt.techin.taskmanager.controller.TaskController",
                "Could not find class 'lt.techin.taskmanager.controller.TaskController'."
        );
        Class<?> createProjectType = loadRequiredClass("lt.techin.taskmanager.dto.CreateProjectRequest", "Missing CreateProjectRequest.");
        Class<?> updateProjectType = loadRequiredClass("lt.techin.taskmanager.dto.UpdateProjectRequest", "Missing UpdateProjectRequest.");
        Class<?> patchProjectType = loadRequiredClass("lt.techin.taskmanager.dto.PatchProjectRequest", "Missing PatchProjectRequest.");
        Class<?> createTaskType = loadRequiredClass("lt.techin.taskmanager.dto.CreateTaskRequest", "Missing CreateTaskRequest.");
        Class<?> updateTaskType = loadRequiredClass("lt.techin.taskmanager.dto.UpdateTaskRequest", "Missing UpdateTaskRequest.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Missing TaskStatus.");

        getRequiredMethod(projectControllerType, "create", new Class<?>[]{createProjectType}, "ProjectController.create(...) should accept CreateProjectRequest.");
        getRequiredMethod(projectControllerType, "update", new Class<?>[]{Long.class, updateProjectType}, "ProjectController.update(...) should accept UpdateProjectRequest.");
        getRequiredMethod(projectControllerType, "patch", new Class<?>[]{Long.class, patchProjectType}, "ProjectController.patch(...) should accept PatchProjectRequest.");
        getRequiredMethod(projectControllerType, "getTasks", new Class<?>[]{Long.class, statusType}, "ProjectController.getTasks(...) should accept project id and optional TaskStatus.");
        getRequiredMethod(projectControllerType, "createTask", new Class<?>[]{Long.class, createTaskType}, "ProjectController.createTask(...) should accept projectId and CreateTaskRequest.");

        getRequiredMethod(taskControllerType, "getById", new Class<?>[]{Long.class}, "TaskController.getById(...) should still work by task id.");
        getRequiredMethod(taskControllerType, "update", new Class<?>[]{Long.class, updateTaskType}, "TaskController.update(...) should accept UpdateTaskRequest.");
        getRequiredMethod(taskControllerType, "updateStatus", new Class<?>[]{Long.class, statusType}, "TaskController.updateStatus(...) should accept TaskStatus.");
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

    private boolean hasMember(Class<?> type, String memberName) {
        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                if (component.getName().equals(memberName)) {
                    return true;
                }
            }
            return false;
        }

        try {
            type.getDeclaredField(memberName);
            return true;
        } catch (NoSuchFieldException exception) {
            return false;
        }
    }

    private void assertHasMember(Class<?> type, String memberName, String failureMessage) {
        if (!hasMember(type, memberName)) {
            fail(failureMessage);
        }
    }

    private void assertValidationAnnotation(Class<?> type, String memberName, String annotationFqcn, String failureMessage) {
        Class<? extends Annotation> annotationType = loadAnnotationType(annotationFqcn);
        assertNotNull(getAnnotation(type, memberName, annotationType), failureMessage);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Annotation> loadAnnotationType(String fqcn) {
        try {
            return (Class<? extends Annotation>) Class.forName(fqcn);
        } catch (ClassNotFoundException exception) {
            fail("Could not load validation annotation type " + fqcn + ".");
            return null;
        }
    }

    private <T extends Annotation> T getAnnotation(Class<?> type, String memberName, Class<T> annotationType) {
        try {
            if (type.isRecord()) {
                for (RecordComponent component : type.getRecordComponents()) {
                    if (component.getName().equals(memberName)) {
                        T annotation = component.getAnnotation(annotationType);
                        if (annotation != null) {
                            return annotation;
                        }
                        annotation = component.getAccessor().getAnnotation(annotationType);
                        if (annotation != null) {
                            return annotation;
                        }
                        return type.getDeclaredField(memberName).getAnnotation(annotationType);
                    }
                }
                return null;
            }

            Field field = type.getDeclaredField(memberName);
            return field.getAnnotation(annotationType);
        } catch (NoSuchFieldException exception) {
            return null;
        }
    }
}
