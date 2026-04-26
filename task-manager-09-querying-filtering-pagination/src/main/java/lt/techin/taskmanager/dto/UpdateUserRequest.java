package lt.techin.taskmanager.dto;

public class UpdateUserRequest {
    private String name;

    private String email;

    public UpdateUserRequest() {
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
