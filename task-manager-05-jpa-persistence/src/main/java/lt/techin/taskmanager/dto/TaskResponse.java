package lt.techin.taskmanager.dto;

import java.time.LocalDate;

public record TaskResponse(Long id, String title, String description, boolean done, LocalDate dueDate) {
}
