package lt.techin.taskmanager.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lt.techin.taskmanager.model.TaskStatus;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @NotBlank
        @Size(min = 3, max = 100)
        String title,

        @NotBlank
        @Size(max = 1000)
        String description,

        @NotNull
        @FutureOrPresent
        LocalDate dueDate,

        @NotNull
        Long projectId,

        @NotNull
        TaskStatus status
) {}
