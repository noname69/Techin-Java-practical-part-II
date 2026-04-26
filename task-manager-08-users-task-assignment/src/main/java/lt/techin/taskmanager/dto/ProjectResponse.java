package lt.techin.taskmanager.dto;

import lt.techin.taskmanager.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        boolean archived
        ) {
}
