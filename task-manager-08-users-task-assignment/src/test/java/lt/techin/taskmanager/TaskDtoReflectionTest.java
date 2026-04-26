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
    void dtoTypesExistAndTaskResponsesExposeProjectAndAssigneeSummaries() {
        Class<?> createUserType = loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateUserRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateUserRequest'."
        );
        Class<?> updateUserType = loadRequiredClass(
                "lt.techin.taskmanager.dto.UpdateUserRequest",
                "Could not find class 'lt.techin.taskmanager.dto.UpdateUserRequest'."
        );
        Class<?> userResponseType = loadRequiredClass(
                "lt.techin.taskmanager.dto.UserResponse",
                "Could not find class 'lt.techin.taskmanager.dto.UserResponse'."
        );
        Class<?> userSummaryType = loadRequiredClass(
                "lt.techin.taskmanager.dto.UserSummaryResponse",
                "Could not find class 'lt.techin.taskmanager.dto.UserSummaryResponse'."
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

        assertHasMember(createTaskType, "assigneeId", "CreateTaskRequest should contain assigneeId so a task can be assigned on creation.");
        assertHasMember(updateTaskType, "assigneeId", "UpdateTaskRequest should contain assigneeId so a task can be reassigned.");
        assertHasMember(taskResponseType, "project", "TaskResponse should still contain project summary data.");
        assertHasMember(taskResponseType, "assignee", "TaskResponse should contain assignee summary data.");
        assertHasMember(userSummaryType, "email", "UserSummaryResponse should contain email.");

        assertValidationAnnotation(createUserType, "name", "jakarta.validation.constraints.NotBlank", "CreateUserRequest.name should have @NotBlank.");
        assertValidationAnnotation(createUserType, "email", "jakarta.validation.constraints.Email", "CreateUserRequest.email should have @Email.");
        assertValidationAnnotation(createTaskType, "dueDate", "jakarta.validation.constraints.FutureOrPresent", "CreateTaskRequest.dueDate should keep @FutureOrPresent.");

        if (hasMember(userResponseType, "tasks")) {
            fail("UserResponse should not expose a full task collection. Keep entities internal and return DTOs only.");
        }
    }

    @Test
    void controllersUseUserAndAssignmentDtoBoundaries() {
        Class<?> userControllerType = loadRequiredClass(
                "lt.techin.taskmanager.controller.UserController",
                "Could not find class 'lt.techin.taskmanager.controller.UserController'."
        );
        Class<?> projectControllerType = loadRequiredClass(
                "lt.techin.taskmanager.controller.ProjectController",
                "Could not find class 'lt.techin.taskmanager.controller.ProjectController'."
        );
        Class<?> taskControllerType = loadRequiredClass(
                "lt.techin.taskmanager.controller.TaskController",
                "Could not find class 'lt.techin.taskmanager.controller.TaskController'."
        );
        Class<?> createUserType = loadRequiredClass("lt.techin.taskmanager.dto.CreateUserRequest", "Missing CreateUserRequest.");
        Class<?> updateUserType = loadRequiredClass("lt.techin.taskmanager.dto.UpdateUserRequest", "Missing UpdateUserRequest.");
        Class<?> createTaskType = loadRequiredClass("lt.techin.taskmanager.dto.CreateTaskRequest", "Missing CreateTaskRequest.");
        Class<?> updateTaskType = loadRequiredClass("lt.techin.taskmanager.dto.UpdateTaskRequest", "Missing UpdateTaskRequest.");
        Class<?> statusType = loadRequiredClass("lt.techin.taskmanager.model.TaskStatus", "Missing TaskStatus.");

        getRequiredMethod(userControllerType, "create", new Class<?>[]{createUserType}, "UserController.create(...) should accept CreateUserRequest.");
        getRequiredMethod(userControllerType, "update", new Class<?>[]{Long.class, updateUserType}, "UserController.update(...) should accept UpdateUserRequest.");
        getRequiredMethod(projectControllerType, "createTask", new Class<?>[]{Long.class, createTaskType}, "ProjectController.createTask(...) should accept CreateTaskRequest.");
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
