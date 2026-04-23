package lt.techin.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateProjectRequest {
    @NotBlank(message = "must not be blank")
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 1000, message = "too long")
    private String description;

    private Boolean archived;


    public CreateProjectRequest() {

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
