package lt.techin.taskmanager.dto;

public class UpdateTaskRequest {
    private String title;
    private String description;
    private boolean done;

    public UpdateTaskRequest() {

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
