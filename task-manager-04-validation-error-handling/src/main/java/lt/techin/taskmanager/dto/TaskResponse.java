package lt.techin.taskmanager.dto;

import java.time.LocalDate;

public class TaskResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final boolean done;
    private final LocalDate dueDate;

    public TaskResponse(Long id, String title, String description, boolean done, LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.done = done;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDone() {
        return done;
    }

    public LocalDate getDueDate() { return dueDate; }
}
