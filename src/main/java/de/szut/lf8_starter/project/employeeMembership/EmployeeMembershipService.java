package de.szut.lf8_starter.project.employeeMembership;

import de.szut.lf8_starter.EmployeeWebServiceAccessPoint.EmployeeReadService;
import de.szut.lf8_starter.exceptionHandling.ResourceNotFoundException;
import de.szut.lf8_starter.exceptionHandling.PlanningConflictException;
import de.szut.lf8_starter.project.ProjectEntity;
import de.szut.lf8_starter.project.ProjectRepository;
import de.szut.lf8_starter.project.employeeMembership.Dtos.AddEmployeeMembershipDto;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeMembershipService {
    private final EmployeeMembershipRepository employeeMembershipRepository;
    private ProjectRepository projectRepository;
    private EmployeeReadService employeeReadService;

    public EmployeeMembershipService(ProjectRepository projectRepository,
                                     EmployeeMembershipRepository employeeMembershipRepository,
                                     EmployeeReadService employeeReadService) {
        this.projectRepository = projectRepository;
        this.employeeReadService = employeeReadService;
        this.employeeMembershipRepository = employeeMembershipRepository;
    }

    public void EnsureAddAllMembersToProjectRequestIsSafe(ProjectEntity projectEntity, Set<AddEmployeeMembershipDto> employees, Set<AddQualificationConnectionDto> qualifications) {
        var allQualificationIds = new HashSet(qualifications.stream().map(AddQualificationConnectionDto::getQualificationId).toList());
        var allProjects = projectRepository.findAll();
        for (var employee:
             employees) {
            if (!allQualificationIds.contains(employee.getQualificationId())) throw new ResourceNotFoundException("project does not require qualification with id: " + employee.getQualificationId());
            if (!employeeReadService.GetRequest(employee.getEmployeeId()).getSkillSet().stream()
                    .map(x -> x.getId()).toList().contains(employee.getQualificationId())) throw new ResourceNotFoundException("employee does not own qualification with id: " + employee.getQualificationId());
            for (var project :
                    allProjects) {
                if (project.getEmployeeMemberships().stream().anyMatch(x -> x.getEmployeeId().equals(employee.getEmployeeId()))) {
                    if (project.getStartDate().before(projectEntity.getEndDate()) && projectEntity.getStartDate().before(project.getEndDate())) {
                        throw new PlanningConflictException("employee with id " + employee.getQualificationId() + " already tied to another project with id " + project.getId());
                    }
                }
            }
        }
    }

    public void AddAllEmployeesToProject(ProjectEntity projectEntity, Set<AddEmployeeMembershipDto> employees, Set<AddQualificationConnectionDto> qualifications) {
        var employeeMemberships = employees.stream().map(x -> {
            var em = new EmployeeMembershipEntity();
            em.setEmployeeId(x.getEmployeeId());
            em.setQualificationId(x.getQualificationId());
            em.setProject(projectEntity);
            return em;
        }).toList();
        employeeMembershipRepository.saveAll(employeeMemberships);
        projectEntity.setEmployeeMemberships(employeeMemberships);
    }
}
