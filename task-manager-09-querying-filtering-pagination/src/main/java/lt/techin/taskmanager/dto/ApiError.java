package lt.techin.taskmanager.dto;

import java.util.Collections;
import java.util.List;

public class ApiError {

    private final int status;
    private final String error;
    private final String message;
    private final List<FieldError> fieldErrors;

    public ApiError(int status, String error, String message) {
        this(status, error, message, Collections.emptyList());
    }

    public ApiError(int status, String error, String message, List<FieldError> fieldErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyList();
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
