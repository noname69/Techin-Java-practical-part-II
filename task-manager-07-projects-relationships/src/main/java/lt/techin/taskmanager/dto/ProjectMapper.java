package lt.techin.taskmanager.dto;

import lt.techin.taskmanager.model.Project;

import java.util.List;

public class ProjectMapper {

    public static Project toProject(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setArchived(false);
        return project;
    }

    public static Project toProject(UpdateProjectRequest request, Long id) {
        Project project = new Project();
        project.setId(id);
        project.setName(request.name());
        project.setDescription(request.description());
        project.setArchived(request.archived());
        return project;
    }

    public static ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.isArchived()
        );
    }

    public static List<ProjectResponse> toResponseList(List<Project> projects) {
        return projects.stream()
                .map(ProjectMapper::toResponse)
                .toList();
    }
}
