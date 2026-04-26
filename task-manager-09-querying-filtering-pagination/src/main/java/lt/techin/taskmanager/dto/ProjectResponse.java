package lt.techin.taskmanager.dto;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        boolean archived
) {
}
