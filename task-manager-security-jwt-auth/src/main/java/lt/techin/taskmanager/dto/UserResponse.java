package lt.techin.taskmanager.dto;

import lt.techin.taskmanager.model.Role;

import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String email,
        List<Role> roles
) {
}
