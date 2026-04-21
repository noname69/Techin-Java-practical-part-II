package lt.techin.taskmanager.service;

import lt.techin.taskmanager.dto.PatchProjectRequest;
import lt.techin.taskmanager.exception.DuplicateProjectNameException;
import lt.techin.taskmanager.exception.ProjectNotFoundException;
import lt.techin.taskmanager.model.Project;
import lt.techin.taskmanager.repository.ProjectRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    public Project getById(Long id) {
        Optional<Project> projectOpt = projectRepository.findById(id);

        if (projectOpt.isPresent()) {
            return projectOpt.get();
        }

        throw new ProjectNotFoundException(id);
    }

    public Project create(Project request) {
        if (projectRepository.existsByName(request.getName())) {
            throw new DuplicateProjectNameException(request.getName());
        }

        Project project = new Project();
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription());

        return projectRepository.save(project);
    }

    public Project update(Long id, Project updatetProject) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        existing.setName(updatetProject.getName());
        existing.setDescription(updatetProject.getDescription());
        existing.setArchived(updatetProject.isArchived());

        return projectRepository.save(existing);
    }

    public void delete(Long id) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        projectRepository.delete(existing);
    }

    public Project updateArchived(Long id, boolean archived) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        project.setArchived(archived);

        return projectRepository.save(project);
    }

    public Project patchArchived(Long id, Boolean archived) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        if (archived != null) {
            project.setArchived(archived);
        }

        return projectRepository.save(project);
    }

//    public Project patch(Long id, PatchProjectRequest request) throws BadRequestException {
//
//        if (request == null || request.archived() == null) {
//            throw new BadRequestException("At least one field must be provided");
//        }
//
//        Project project = projectRepository.findById(id)
//                .orElseThrow(() -> new ProjectNotFoundException(id));
//
//        project.setArchived(request.archived());
//
//        return projectRepository.save(project);
//    }
}
