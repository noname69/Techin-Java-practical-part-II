package lt.techin.taskmanager.exception;

public class IllegalTaskStateException extends RuntimeException {
    public IllegalTaskStateException(Long id) {

        super("Task with id " + id + " is already done.");
    }
}
