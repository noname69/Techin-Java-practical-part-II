package lt.techin.taskmanager.dto;

public class CreateTaskRequest {
    private String title;
    private String description;

    public CreateTaskRequest() {

    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
