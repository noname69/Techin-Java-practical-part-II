package lt.techin.taskmanager.exception;

import lt.techin.taskmanager.dto.ApiError;
import lt.techin.taskmanager.dto.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(TaskNotFoundException ex) {
        ApiError error = new ApiError(404, "Not Found", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalTaskStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalTaskStateException ex) {
        ApiError error = new ApiError(409, "Conflict", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiError> handlerProjectNotFound(ProjectNotFoundException ex) {
        ApiError error = new ApiError(404, "Not Found", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateProjectNameException.class)
    public ResponseEntity<ApiError> handleDuplicateProjectName(DuplicateProjectNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, "Conflict", ex.getMessage()
                ));
    }

    @ExceptionHandler(ArchivedProjectException.class)
    public ResponseEntity<ApiError> handleArchivedProject(ArchivedProjectException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, "Conflict", ex.getMessage()
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleDuplicate(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(401, "Invalid credentials", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validation failed.",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
}
