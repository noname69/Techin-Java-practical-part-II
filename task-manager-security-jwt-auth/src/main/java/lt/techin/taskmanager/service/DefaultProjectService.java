package lt.techin.taskmanager.service;

import lt.techin.taskmanager.dto.PatchProjectRequest;
import lt.techin.taskmanager.exception.DuplicateProjectNameException;
import lt.techin.taskmanager.exception.ProjectNotFoundException;
import lt.techin.taskmanager.model.Project;
import lt.techin.taskmanager.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DefaultProjectService implements ProjectService {
    private final ProjectRepository projectRepository;

    public DefaultProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // GET all projects
    @Override
    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    // GET projects by ID
    @Override
    public Project getById(Long id) {
        Optional<Project> projectOpt = projectRepository.findById(id);

        if (projectOpt.isPresent()) {
            return projectOpt.get();
        }

        throw new ProjectNotFoundException(id);
    }

    //CREATE new project
    @Override
    public Project create(Project project) {
        if (projectRepository.existsByName(project.getName())) {
            throw new DuplicateProjectNameException(project.getName());
        }
        Project newProject = new Project();
        newProject.setName(project.getName());
        newProject.setDescription(project.getDescription());

        return projectRepository.save(newProject);
    }

    //UPDATE project
    @Override
    public Project update(Long id, Project updatedProject) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        existing.setName(updatedProject.getName());
        existing.setDescription(updatedProject.getDescription());
        existing.setArchived(updatedProject.isArchived());

        return projectRepository.save(existing);
    }

    //PATCH project
    @Override
    public Project patch(Long id, PatchProjectRequest patchedProject) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        existing.setArchived(patchedProject.archived());

        return projectRepository.save(existing);
    }

    //DELETE project
    @Override
    public void delete(Long id) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        projectRepository.delete(existing);
    }

    //UPDATE archived
    @Override
    public Project updateArchived(Long id, boolean archived) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        project.setArchived(archived);

        return projectRepository.save(project);
    }
}
