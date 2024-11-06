package de.szut.lf8_starter.project;

import de.szut.lf8_starter.exceptionHandling.DateConflictException;
import de.szut.lf8_starter.exceptionHandling.ResourceNotFoundException;
import de.szut.lf8_starter.project.dtos.AddProjectDto;
import de.szut.lf8_starter.project.dtos.UpdateProjectDto;
import de.szut.lf8_starter.project.employeeMembership.Dtos.AddEmployeeMembershipDto;
import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipService;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionIndividualDto;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final EmployeeMembershipService employeeMembershipService;
    private final QualificationConnectionService qualificationConnectionService;

    public ProjectService(ProjectRepository projectRepository,
                          EmployeeMembershipService employeeMembershipService,
                          QualificationConnectionService qualificationConnectionService) {
        this.projectRepository = projectRepository;
        this.employeeMembershipService = employeeMembershipService;
        this.qualificationConnectionService = qualificationConnectionService;
    }

    public ProjectEntity createProject(AddProjectDto addProjectDto) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(addProjectDto.getName());

        if (addProjectDto.getPlannedEndDate().before(addProjectDto.getStartDate()))
            throw new DateConflictException("end date shouldn't be before start date");
        projectEntity.setStartDate(addProjectDto.getStartDate());
        projectEntity.setPlannedEndDate(addProjectDto.getPlannedEndDate());
        projectEntity.setCustomerId(addProjectDto.getCustomerId());
        projectEntity.setContactPersonName(addProjectDto.getContactPersonName());
        projectEntity.setComment(addProjectDto.getComment());

        qualificationConnectionService.ensureAddAllQualificationConnectionsToProjectIsSafe(projectEntity, addProjectDto.getQualifications());
        employeeMembershipService.ensureAddAllMembersToProjectRequestIsSafe(projectEntity, addProjectDto.getEmployees(), addProjectDto.getQualifications());

        projectEntity = projectRepository.save(projectEntity);

        qualificationConnectionService.addAllConnectionsToProject(projectEntity, addProjectDto.getQualifications());
        employeeMembershipService.addAllEmployeesToProject(projectEntity, addProjectDto.getEmployees(), addProjectDto.getQualifications());

        return projectEntity;
    }

    public void delete(Long id) {
        if (projectRepository.findById(id).isEmpty())
            throw new ResourceNotFoundException("project with id " + id + " doesnt exist");
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
        if (employeeMembershipService.findById(id) == null)
            throw new ResourceNotFoundException("employee does not exist with id " + id);
        return employeeMembershipService.getAllProjectsByEmployeeId(id);
    }

    public ProjectEntity updateById(Long id, UpdateProjectDto updateProjectDto) {
        var response = projectRepository.findById(id);
        if (response.isEmpty()) throw new ResourceNotFoundException("project with id " + id + "does not exist");

        var entity = response.get();

        employeeMembershipService.ensureUpdateDateOnProjectIsSafe(entity, updateProjectDto.getStartDate(), updateProjectDto.getPlannedEndDate(), updateProjectDto.getActualEndDate());

        entity.setName(updateProjectDto.getName());
        entity.setStartDate(updateProjectDto.getStartDate());
        entity.setPlannedEndDate(updateProjectDto.getPlannedEndDate());
        entity.setActualEndDate(updateProjectDto.getActualEndDate());
        entity.setCustomerId(updateProjectDto.getCustomerId());
        entity.setContactPersonName(updateProjectDto.getContactPersonName());
        entity.setComment(updateProjectDto.getComment());

        return projectRepository.save(entity);
    }

    public ProjectEntity AddEmployeeToProject(Long projectId, AddEmployeeMembershipDto addDto) {
        employeeMembershipService.ensureAddMemberToProjectRequestIsSafe(projectId, addDto.getEmployeeId(), addDto.getQualificationId());
        return employeeMembershipService.addMemberToProject(projectId, addDto.getEmployeeId(), addDto.getQualificationId());
    }

    public void RemoveEmployeeFromProject(Long projectId, Long employeeId) {
        employeeMembershipService.ensureRemoveMemberFromProjectRequestIsSafe(projectId, employeeId);
        employeeMembershipService.removeEmployeeFromProject(projectId, employeeId);
    }

    public ProjectEntity AddQualificationToProject(Long projectId, AddQualificationConnectionIndividualDto addQualificationConnectionIndividualDto) {
        qualificationConnectionService.ensureAddQualificationToProjectRequestIsSafe(projectId, addQualificationConnectionIndividualDto.getQualificationId());
        return qualificationConnectionService.addQualificationToProject(projectId, addQualificationConnectionIndividualDto.getQualificationId());
    }

    public void RemoveQualificationFromProject(Long projectId, Long qualificationId) {
        qualificationConnectionService.ensureRemoveQualificationFromProjectRequestIsSafe(projectId, qualificationId);
        qualificationConnectionService.removeQualificationFromProject(projectId, qualificationId);
    }
}
