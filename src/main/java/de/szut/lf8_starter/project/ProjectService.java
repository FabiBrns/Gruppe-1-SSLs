package de.szut.lf8_starter.project;

import de.szut.lf8_starter.exceptionHandling.DateConflictException;
import de.szut.lf8_starter.exceptionHandling.ResourceNotFoundException;
import de.szut.lf8_starter.project.dtos.AddProjectDto;
import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipService;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionService;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public ProjectEntity CreateProject(AddProjectDto addProjectDto) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(addProjectDto.getName());

        if (addProjectDto.getEndDate().before(addProjectDto.getStartDate())) throw new DateConflictException("end date shouldn't be before start date");
        projectEntity.setStartDate(addProjectDto.getStartDate());
        projectEntity.setEndDate(addProjectDto.getEndDate());

        qualificationConnectionService.EnsureAddAllQualificationConnectionsToProjectIsSafe(projectEntity, addProjectDto.getQualifications());
        employeeMembershipService.EnsureAddAllMembersToProjectRequestIsSafe(projectEntity, addProjectDto.getEmployees(), addProjectDto.getQualifications());

        projectEntity = projectRepository.save(projectEntity);

        qualificationConnectionService.AddAllConnectionsToProject(projectEntity, addProjectDto.getQualifications());
        employeeMembershipService.AddAllEmployeesToProject(projectEntity, addProjectDto.getEmployees(), addProjectDto.getQualifications());

        return projectEntity;
    }

    public void delete(Long id) {
        if (projectRepository.findById(id).isEmpty()) throw new ResourceNotFoundException("project with id " + id + " doesnt exist");
        projectRepository.deleteById(id);
    }

    public List<ProjectEntity> getAll() {
        return projectRepository.findAll();
    }

    public ProjectEntity getById(Long id) {
        var response = projectRepository.findById(id);
        if (response.isEmpty()) throw new ResourceNotFoundException("no project found with id " + id);
        return response.get();
    }

    public List<ProjectEntity> getAllByEmployeeId(Long id) {
        if (employeeMembershipService.findById(id) == null) throw new ResourceNotFoundException("employee does not exist with id " + id);
        return employeeMembershipService.getAllProjectsByEmployeeId(id);
    }
}
