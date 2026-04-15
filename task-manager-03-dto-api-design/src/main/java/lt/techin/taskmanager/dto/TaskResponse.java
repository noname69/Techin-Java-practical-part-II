package lt.techin.taskmanager.dto;

public class TaskResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final boolean done;

    public TaskResponse(Long id, String title, String description, boolean done) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.done = done;
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
}
