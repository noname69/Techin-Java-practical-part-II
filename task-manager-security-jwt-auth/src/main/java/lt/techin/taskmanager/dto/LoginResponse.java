package lt.techin.taskmanager.dto;

import java.util.List;

public record LoginResponse(

        String tokenType,
        String accessToken,
        String email,
        List<String> roles
) {}
