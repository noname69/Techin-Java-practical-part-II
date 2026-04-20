package lt.techin.taskmanager;

import lt.techin.taskmanager.controller.TaskController;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TaskDtoReflectionTest {

    @Test
    void requestAndResponseDtosStillExist() {
        loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateTaskRequest'. Keep the DTO-based API boundary from Task 03."
        );
        loadRequiredClass(
                "lt.techin.taskmanager.dto.UpdateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.UpdateTaskRequest'. Keep the DTO-based API boundary from Task 03."
        );
        loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskResponse",
                "Could not find class 'lt.techin.taskmanager.dto.TaskResponse'. Keep the DTO-based API boundary from Task 03."
        );
    }

    @Test
    void requestDtosContainValidationAnnotationsAndDueDate() {
        Class<?> createRequestType = loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateTaskRequest'."
        );
        Class<?> updateRequestType = loadRequiredClass(
                "lt.techin.taskmanager.dto.UpdateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.UpdateTaskRequest'."
        );
        Class<?> responseType = loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskResponse",
                "Could not find class 'lt.techin.taskmanager.dto.TaskResponse'."
        );

        assertValidationAnnotations(
                createRequestType,
                "CreateTaskRequest should validate title with @NotBlank and @Size(min = 3, max = 100), description with @Size(max = 1000), and dueDate with @NotNull and @FutureOrPresent."
        );
        assertValidationAnnotations(
                updateRequestType,
                "UpdateTaskRequest should validate title with @NotBlank and @Size(min = 3, max = 100), description with @Size(max = 1000), and dueDate with @NotNull and @FutureOrPresent."
        );
        assertLocalDateMember(responseType, "dueDate", "TaskResponse should include dueDate as LocalDate.");
    }

    @Test
    void controllerUsesValidOnCreateAndUpdateRequestBodies() {
        Class<?> createRequestType = loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateTaskRequest'."
        );
        Class<?> updateRequestType = loadRequiredClass(
                "lt.techin.taskmanager.dto.UpdateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.UpdateTaskRequest'."
        );

        Method createMethod = getRequiredMethod(
                TaskController.class,
                "create",
                new Class<?>[]{createRequestType},
                "TaskController.create(...) should accept CreateTaskRequest."
        );
        Method updateMethod = getRequiredMethod(
                TaskController.class,
                "update",
                new Class<?>[]{Long.class, updateRequestType},
                "TaskController.update(...) should accept UpdateTaskRequest."
        );

        Parameter createParameter = createMethod.getParameters()[0];
        Parameter updateParameter = updateMethod.getParameters()[1];

        assertHasAnnotationByName(
                createParameter,
                "jakarta.validation.Valid",
                "TaskController.create(...) should put @Valid on the request body parameter so invalid create requests return 400."
        );
        assertHasAnnotationByName(
                updateParameter,
                "jakarta.validation.Valid",
                "TaskController.update(...) should put @Valid on the request body parameter so invalid update requests return 400."
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

    private void assertValidationAnnotations(Class<?> type, String failureMessage) {
        Class<? extends Annotation> notBlankType = loadAnnotationType("jakarta.validation.constraints.NotBlank", failureMessage);
        Class<? extends Annotation> notNullType = loadAnnotationType("jakarta.validation.constraints.NotNull", failureMessage);
        Class<? extends Annotation> futureOrPresentType = loadAnnotationType("jakarta.validation.constraints.FutureOrPresent", failureMessage);
        Class<? extends Annotation> sizeType = loadAnnotationType("jakarta.validation.constraints.Size", failureMessage);

        assertHasAnnotation(type, "title", notBlankType, failureMessage);
        Annotation titleSize = getAnnotation(type, "title", sizeType);
        assertNotNull(titleSize, failureMessage);
        assertTrue(getIntAttribute(titleSize, "min") == 3 && getIntAttribute(titleSize, "max") == 100, failureMessage);

        Annotation descriptionSize = getAnnotation(type, "description", sizeType);
        assertNotNull(descriptionSize, failureMessage);
        assertTrue(getIntAttribute(descriptionSize, "max") == 1000, failureMessage);

        assertHasAnnotation(type, "dueDate", notNullType, failureMessage);
        assertHasAnnotation(type, "dueDate", futureOrPresentType, failureMessage);
        assertLocalDateMember(type, "dueDate", failureMessage);
    }

    private void assertHasAnnotation(Class<?> type, String memberName, Class<? extends Annotation> annotationType, String failureMessage) {
        assertNotNull(getAnnotation(type, memberName, annotationType), failureMessage);
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

                        Field field = type.getDeclaredField(memberName);
                        return field.getAnnotation(annotationType);
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

    @SuppressWarnings("unchecked")
    private Class<? extends Annotation> loadAnnotationType(String fqcn, String failureMessage) {
        try {
            return (Class<? extends Annotation>) Class.forName(fqcn);
        } catch (ClassNotFoundException exception) {
            fail(failureMessage);
            return null;
        }
    }

    private int getIntAttribute(Annotation annotation, String attributeName) {
        try {
            return (Integer) annotation.annotationType().getMethod(attributeName).invoke(annotation);
        } catch (ReflectiveOperationException exception) {
            fail("Could not read annotation attribute '" + attributeName + "'.");
            return -1;
        }
    }

    private void assertHasAnnotationByName(Parameter parameter, String annotationFqcn, String failureMessage) {
        for (Annotation annotation : parameter.getAnnotations()) {
            if (annotation.annotationType().getName().equals(annotationFqcn)) {
                return;
            }
        }
        fail(failureMessage);
    }

    private void assertLocalDateMember(Class<?> type, String memberName, String failureMessage) {
        try {
            if (type.isRecord()) {
                for (RecordComponent component : type.getRecordComponents()) {
                    if (component.getName().equals(memberName)) {
                        assertTrue(component.getType().equals(LocalDate.class), failureMessage);
                        return;
                    }
                }
                fail(failureMessage);
                return;
            }

            Field field = type.getDeclaredField(memberName);
            assertTrue(field.getType().equals(LocalDate.class), failureMessage);
        } catch (NoSuchFieldException exception) {
            fail(failureMessage);
        }
    }
}
