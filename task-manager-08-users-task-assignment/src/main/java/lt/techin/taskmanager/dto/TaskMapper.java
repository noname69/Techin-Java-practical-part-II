package lt.techin.taskmanager.dto;

import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
import lt.techin.taskmanager.model.User;

import java.util.List;

public class TaskMapper {
    public static Task toTask(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.TODO);
        task.setDueDate(request.getDueDate());
        return task;
    }

    public static Task toTask(UpdateTaskRequest request, Long id) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        return task;
    }

    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getCompletedAt(),
                new ProjectSummaryResponse(
                        task.getProject().getId(),
                        task.getProject().getName()
                ),
                toUserSummary(task.getAssignee())
        );
    }

    public static List<TaskResponse> toResponseList(List<Task> tasks) {
        return tasks.stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    private static UserSummaryResponse toUserSummary(User user) {
        if (user == null) return null;

        return new UserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
