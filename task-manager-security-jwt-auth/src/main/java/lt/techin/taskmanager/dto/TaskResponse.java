package lt.techin.taskmanager.dto;

import lt.techin.taskmanager.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate,
        LocalDateTime completedAt,
        ProjectSummaryResponse project,
        UserSummaryResponse assignee
) {
}
