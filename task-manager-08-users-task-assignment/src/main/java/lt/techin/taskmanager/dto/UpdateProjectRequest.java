package lt.techin.taskmanager.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lt.techin.taskmanager.model.TaskStatus;

import java.time.LocalDate;

public class UpdateProjectRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    private Boolean archived;

    public UpdateProjectRequest() {

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean isArchived() {
        return archived;
    }
}
