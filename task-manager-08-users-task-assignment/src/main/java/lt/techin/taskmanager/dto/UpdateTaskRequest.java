package lt.techin.taskmanager.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;

import java.time.LocalDate;

public class UpdateTaskRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotNull
    private TaskStatus status;

    @NotNull
    @FutureOrPresent
    private LocalDate dueDate;

    @NotNull
    private Long projectId;

    private Long assigneeId;

    public UpdateTaskRequest() {

    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }
}
