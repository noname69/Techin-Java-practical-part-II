package lt.techin.taskmanager.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateTaskRequest {

    @NotBlank(message = "must not be blank")
    @Size(min = 3, max = 100)
    private String title;

    @Size(max = 1000, message = "too long")
    private String description;

    @NotNull(message = "must not be null")
    @FutureOrPresent
    private LocalDate dueDate;

    private Long assigneeId;

    public CreateTaskRequest() {

    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() { return dueDate; }

    public Long getAssigneeId() {
        return assigneeId;
    }
}
