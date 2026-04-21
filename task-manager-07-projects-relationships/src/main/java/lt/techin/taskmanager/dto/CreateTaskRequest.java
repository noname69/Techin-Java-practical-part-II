package lt.techin.taskmanager.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(

        @NotBlank(message = "must not be blank")
        @Size(min = 3, max = 100)
        String title,

        @Size(max = 1000, message = "too long")
        String description,

        @NotNull(message = "must not be null")
        @FutureOrPresent
        LocalDate dueDate
) {}
