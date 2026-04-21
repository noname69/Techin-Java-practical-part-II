package lt.techin.taskmanager;

import lt.techin.taskmanager.controller.TaskController;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class TaskDtoReflectionTest {

    @Test
    void dtoTypesExistAndResponseIncludesLifecycleFields() {
        Class<?> createType = loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateTaskRequest'."
        );
        Class<?> updateType = loadRequiredClass(
                "lt.techin.taskmanager.dto.UpdateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.UpdateTaskRequest'."
        );
        Class<?> responseType = loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskResponse",
                "Could not find class 'lt.techin.taskmanager.dto.TaskResponse'."
        );
        loadRequiredClass(
                "lt.techin.taskmanager.model.TaskStatus",
                "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'."
        );

        assertHasMember(updateType, "status", "UpdateTaskRequest should contain a status field or record component.");
        assertHasMember(responseType, "status", "TaskResponse should contain a status field or record component.");
        assertHasMember(responseType, "completedAt", "TaskResponse should contain completedAt so clients can see when the task was completed.");
        assertValidationAnnotations(createType);
    }

    @Test
    void controllerUsesUpdatedDtoBoundary() {
        Class<?> createType = loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateTaskRequest'."
        );
        Class<?> updateType = loadRequiredClass(
                "lt.techin.taskmanager.dto.UpdateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.UpdateTaskRequest'."
        );
        Class<?> statusType = loadRequiredClass(
                "lt.techin.taskmanager.model.TaskStatus",
                "Could not find enum 'lt.techin.taskmanager.model.TaskStatus'."
        );

        getRequiredMethod(
                TaskController.class,
                "create",
                new Class<?>[]{createType},
                "TaskController.create(...) should accept CreateTaskRequest."
        );
        getRequiredMethod(
                TaskController.class,
                "update",
                new Class<?>[]{Long.class, updateType},
                "TaskController.update(...) should accept UpdateTaskRequest."
        );
        getRequiredMethod(
                TaskController.class,
                "searchByStatus",
                new Class<?>[]{statusType},
                "TaskController.searchByStatus(...) should accept TaskStatus."
        );
        getRequiredMethod(
                TaskController.class,
                "updateStatus",
                new Class<?>[]{Long.class, statusType},
                "TaskController.updateStatus(...) should accept TaskStatus."
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

    private Method getRequiredMethod(Class<?> type, String name, Class<?>[] parameterTypes, String failureMessage) {
        try {
            return type.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException exception) {
            fail(failureMessage);
            return null;
        }
    }

    private void assertHasMember(Class<?> type, String memberName, String failureMessage) {
        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                if (component.getName().equals(memberName)) {
                    return;
                }
            }
            fail(failureMessage);
            return;
        }

        try {
            type.getDeclaredField(memberName);
        } catch (NoSuchFieldException exception) {
            fail(failureMessage);
        }
    }

    private void assertValidationAnnotations(Class<?> type) {
        Class<? extends Annotation> notBlankType = loadAnnotationType("jakarta.validation.constraints.NotBlank");
        Class<? extends Annotation> sizeType = loadAnnotationType("jakarta.validation.constraints.Size");
        Class<? extends Annotation> notNullType = loadAnnotationType("jakarta.validation.constraints.NotNull");
        Class<? extends Annotation> futureOrPresentType = loadAnnotationType("jakarta.validation.constraints.FutureOrPresent");

        assertNotNull(getAnnotation(type, "title", notBlankType), "CreateTaskRequest.title should keep validation annotations.");
        assertNotNull(getAnnotation(type, "title", sizeType), "CreateTaskRequest.title should keep @Size validation.");
        assertNotNull(getAnnotation(type, "dueDate", notNullType), "CreateTaskRequest.dueDate should keep @NotNull.");
        assertNotNull(getAnnotation(type, "dueDate", futureOrPresentType), "CreateTaskRequest.dueDate should keep @FutureOrPresent.");
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
