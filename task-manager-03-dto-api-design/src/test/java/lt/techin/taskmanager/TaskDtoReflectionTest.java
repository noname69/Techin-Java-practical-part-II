package lt.techin.taskmanager;

import lt.techin.taskmanager.controller.TaskController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class TaskDtoReflectionTest {

    @Test
    void dtoClassesAndMapperExist() {
        loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateTaskRequest'. Create this request DTO in the dto package."
        );
        loadRequiredClass(
                "lt.techin.taskmanager.dto.UpdateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.UpdateTaskRequest'. Create this request DTO in the dto package."
        );
        loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskResponse",
                "Could not find class 'lt.techin.taskmanager.dto.TaskResponse'. Create this response DTO in the dto package."
        );
        loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskMapper",
                "Could not find class 'lt.techin.taskmanager.dto.TaskMapper'. Create a dedicated manual mapper in the dto package."
        );
    }

    @Test
    void mapperContainsRequiredMethods() {
        Class<?> createRequestType = loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateTaskRequest'."
        );
        Class<?> updateRequestType = loadRequiredClass(
                "lt.techin.taskmanager.dto.UpdateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.UpdateTaskRequest'."
        );
        Class<?> taskResponseType = loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskResponse",
                "Could not find class 'lt.techin.taskmanager.dto.TaskResponse'."
        );
        Class<?> taskType = loadRequiredClass(
                "lt.techin.taskmanager.model.Task",
                "Could not find class 'lt.techin.taskmanager.model.Task'."
        );
        Class<?> mapperType = loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskMapper",
                "Could not find class 'lt.techin.taskmanager.dto.TaskMapper'."
        );

        getRequiredMethod(
                mapperType,
                "toTask",
                new Class<?>[]{createRequestType},
                "TaskMapper must declare method 'toTask(CreateTaskRequest request)'."
        );
        getRequiredMethod(
                mapperType,
                "toTask",
                new Class<?>[]{updateRequestType, Long.class},
                "TaskMapper must declare method 'toTask(UpdateTaskRequest request, Long id)' or an equivalent update mapping helper with exactly these parameters."
        );
        getRequiredMethod(
                mapperType,
                "toResponse",
                new Class<?>[]{taskType},
                "TaskMapper must declare method 'toResponse(Task task)'."
        );

        Method toResponseList = getRequiredMethod(
                mapperType,
                "toResponseList",
                new Class<?>[]{List.class},
                "TaskMapper must declare method 'toResponseList(List<Task> tasks)'."
        );
        assertEquals(
                taskResponseType,
                toResponseList.getGenericReturnType() instanceof ParameterizedType parameterizedType
                        ? parameterizedType.getActualTypeArguments()[0]
                        : null,
                "TaskMapper.toResponseList(...) should return List<TaskResponse>."
        );
    }

    @Test
    void controllerUsesDtosAtHttpBoundary() {
        Class<?> createRequestType = loadRequiredClass(
                "lt.techin.taskmanager.dto.CreateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.CreateTaskRequest'."
        );
        Class<?> updateRequestType = loadRequiredClass(
                "lt.techin.taskmanager.dto.UpdateTaskRequest",
                "Could not find class 'lt.techin.taskmanager.dto.UpdateTaskRequest'."
        );
        Class<?> taskResponseType = loadRequiredClass(
                "lt.techin.taskmanager.dto.TaskResponse",
                "Could not find class 'lt.techin.taskmanager.dto.TaskResponse'."
        );

        Method createMethod = getRequiredMethod(
                TaskController.class,
                "create",
                new Class<?>[]{createRequestType},
                "TaskController.create(...) should accept CreateTaskRequest instead of the internal Task model."
        );
        Method updateMethod = getRequiredMethod(
                TaskController.class,
                "update",
                new Class<?>[]{Long.class, updateRequestType},
                "TaskController.update(...) should accept UpdateTaskRequest instead of the internal Task model."
        );
        Method getAllMethod = getRequiredMethod(
                TaskController.class,
                "getAll",
                new Class<?>[]{},
                "TaskController must still declare getAll()."
        );
        Method getByIdMethod = getRequiredMethod(
                TaskController.class,
                "getById",
                new Class<?>[]{Long.class},
                "TaskController must still declare getById(Long id)."
        );
        Method updateDoneMethod = getRequiredMethod(
                TaskController.class,
                "updateDone",
                new Class<?>[]{Long.class, boolean.class},
                "TaskController must still declare updateDone(Long id, boolean value)."
        );

        assertNotNull(createMethod, "TaskController.create(...) should exist.");
        assertNotNull(updateMethod, "TaskController.update(...) should exist.");
        assertListElementType(
                getAllMethod.getGenericReturnType(),
                taskResponseType,
                "TaskController.getAll() should return List<TaskResponse>."
        );
        assertEquals(
                "org.springframework.http.ResponseEntity<lt.techin.taskmanager.dto.TaskResponse>",
                getByIdMethod.getGenericReturnType().getTypeName(),
                "TaskController.getById(Long) should return ResponseEntity<TaskResponse>."
        );
        assertEquals(
                "org.springframework.http.ResponseEntity<lt.techin.taskmanager.dto.TaskResponse>",
                updateDoneMethod.getGenericReturnType().getTypeName(),
                "TaskController.updateDone(Long, boolean) should return ResponseEntity<TaskResponse>."
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

    private void assertListElementType(Type genericReturnType, Class<?> expectedElementType, String failureMessage) {
        if (!(genericReturnType instanceof ParameterizedType parameterizedType)) {
            fail(failureMessage);
            return;
        }

        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length != 1 || !expectedElementType.getName().equals(typeArguments[0].getTypeName())) {
            fail(failureMessage);
        }
    }
}
