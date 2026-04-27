package lt.techin.taskmanager.service;

import lt.techin.taskmanager.dto.PatchProjectRequest;
import lt.techin.taskmanager.model.Project;

import java.util.List;

public interface ProjectService {

    List<Project> getAll();

    Project getById(Long id);

    Project create(Project project);

    Project update(Long id, Project updatedProject);

    Project patch(Long id, PatchProjectRequest patchedProject);

    void delete(Long id);

    Project updateArchived(Long id, boolean archived);
}
