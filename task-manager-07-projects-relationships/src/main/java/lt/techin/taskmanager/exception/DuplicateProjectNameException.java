package lt.techin.taskmanager.exception;

public class DuplicateProjectNameException extends RuntimeException {
    public DuplicateProjectNameException(String name) {
        super("Project with name '" + name + "' already exists.");
    }
}
