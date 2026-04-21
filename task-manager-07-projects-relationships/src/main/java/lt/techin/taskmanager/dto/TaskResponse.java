package lt.techin.taskmanager.dto;

import lt.techin.taskmanager.model.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        java.time.LocalDate dueDate,
        java.time.LocalDateTime completedAt,
        ProjectSummaryResponse project
) {}
