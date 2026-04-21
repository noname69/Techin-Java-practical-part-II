package lt.techin.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank
        @Size(min = 3, max = 100)
        String name,

        @Size(max = 1000)
        String description


) {}
