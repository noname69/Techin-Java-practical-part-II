package lt.techin.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {

        super("User with id " + id + " was not found.");
    }

    public UserNotFoundException(String email) {
        super("User with email " + email + " was not found.");
    }
}
