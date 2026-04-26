package lt.techin.taskmanager.exception;

public class IllegalTaskStateException extends RuntimeException {
    public IllegalTaskStateException(String message) {
        super(message);
    }
}
