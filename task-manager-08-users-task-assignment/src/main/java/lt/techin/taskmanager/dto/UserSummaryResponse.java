package lt.techin.taskmanager.dto;

    public record UserSummaryResponse(
            Long id,
            String name,
            String email
    ) {}

