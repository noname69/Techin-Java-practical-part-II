package lt.techin.taskmanager.exception;

public class ArchivedProjectException extends RuntimeException {

    public ArchivedProjectException(Long projectId) {
        super("Project with id " + projectId + " is archived and cannot accept new tasks.");
    }
}
