package lt.techin.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank
    @Email
    private String email;

    public CreateUserRequest() {
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
