package lt.techin.taskmanager.dto;

public record UpdateProjectRequest(
        String name,
        String description,
        Boolean archived
) {}
