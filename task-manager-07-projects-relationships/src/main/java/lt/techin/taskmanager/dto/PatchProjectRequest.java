package lt.techin.taskmanager.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record PatchProjectRequest(

        String name,
        String description,
        Boolean archived
) {

    @AssertTrue(message = "At least one field must be provided")
    public boolean isValid() {
        if (name != null && name.trim().isEmpty()) return false;
        return name != null || description != null || archived != null;
    }
}

