package lt.techin.taskmanager.dto;

import lt.techin.taskmanager.model.Project;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;

import java.util.List;

public class TaskMapper {

    public static Task toTask(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.TODO);
        task.setDueDate(request.dueDate());
        return task;
    }

    public static Task toTask(UpdateTaskRequest request, Long id) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setStatus(request.status());
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
                task.getProject() != null ? toProjectSummary(task.getProject()) : null
        );
    }

    public static List<TaskResponse> toResponseList(List<Task> tasks) {
        return tasks.stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    public static ProjectSummaryResponse toProjectSummary(Project project) {
        return new ProjectSummaryResponse(
                project.getId(),
                project.getName(),
                project.isArchived()
        );
    }
}
