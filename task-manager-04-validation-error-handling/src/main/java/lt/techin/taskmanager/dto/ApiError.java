package lt.techin.taskmanager.dto;

import java.util.List;

public class ApiError {
    private final int status;
    private final String error;
    private final String message;
    List<FieldError> fieldErrors;

    public ApiError(int status, String error, String message ) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public ApiError(int status, String error, String message, List<FieldError> fieldErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
