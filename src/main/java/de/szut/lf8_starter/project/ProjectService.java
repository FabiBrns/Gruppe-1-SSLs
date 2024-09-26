package de.szut.lf8_starter.project;

import de.szut.lf8_starter.exceptionHandling.ResourceNotFoundException;
import de.szut.lf8_starter.project.dtos.AddProjectDto;
import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipService;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionService;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    private ProjectRepository projectRepository;
    private EmployeeMembershipService employeeMembershipService;
    private QualificationConnectionService qualificationConnectionService;

    public ProjectService(ProjectRepository projectRepository,
                          EmployeeMembershipService employeeMembershipService,
                          QualificationConnectionService qualificationConnectionService) {
        this.projectRepository = projectRepository;
        this.employeeMembershipService = employeeMembershipService;
        this.qualificationConnectionService = qualificationConnectionService;
    }

    public ProjectEntity CreateProject(AddProjectDto addProjectDto) throws Exception {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(addProjectDto.getName());

        if (addProjectDto.getEndDate().before(addProjectDto.getStartDate())) throw new Exception();
        projectEntity.setStartDate(addProjectDto.getStartDate());
        projectEntity.setEndDate(addProjectDto.getEndDate());

        if (!qualificationConnectionService.CanAddAllConnectionsToProject(projectEntity, addProjectDto.getQualifications()) || !employeeMembershipService.CanAddAllMembersToProject(projectEntity, addProjectDto.getEmployees(), addProjectDto.getQualifications())) throw new Exception();

        projectEntity = projectRepository.save(projectEntity);
        qualificationConnectionService.AddAllConnectionsToProject(projectEntity, addProjectDto.getQualifications());
        employeeMembershipService.AddAllEmployeesToProject(projectEntity, addProjectDto.getEmployees(), addProjectDto.getQualifications());

        return projectEntity;
    }

    public void delete(Long id) {
        if (projectRepository.findById(id).isEmpty()) throw new ResourceNotFoundException("project with id " + id + " doesnt exist");
        projectRepository.deleteById(id);
    }
}
