package de.szut.lf8_starter.project.employeeMembership;

import de.szut.lf8_starter.EmployeeWebServiceAccessPoint.Dtos.GetEmployeeDto;
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

    public void ensureUpdateDateOnProjectIsSafe(ProjectEntity entity, Date startDate, Date endDate) {
        var existentEmployeeMembershipsIds = entity.getEmployeeMemberships();
        for (var membership :
                existentEmployeeMembershipsIds) {
            var allMembershipsForEmployee = employeeMembershipRepository.findAllByEmployeeId(membership.getEmployeeId()).stream().filter(x -> x.getProject().getId() != entity.getId()).toList();
            for (var membershipToCheckForConflictsWith :
                    allMembershipsForEmployee) {

                if (membershipToCheckForConflictsWith.getProject().getStartDate().before(startDate) && startDate.before(membershipToCheckForConflictsWith.getProject().getEndDate())) {
                    throw new PlanningConflictException("employee with id " + membershipToCheckForConflictsWith.getEmployeeId() + " already tied to another project with id " + membershipToCheckForConflictsWith.getProject().getId());
                }
            }
        }
    }

    public void EnsureAddAllMembersToProjectRequestIsSafe(ProjectEntity projectEntity, Set<AddEmployeeMembershipDto> employees, Set<AddQualificationConnectionDto> qualifications) {
        var allQualificationIds = new HashSet(qualifications.stream().map(AddQualificationConnectionDto::getQualificationId).toList());
        var allProjects = projectRepository.findAll();
        for (var employee:
             employees) {
            if (!allQualificationIds.contains(employee.getQualificationId())) throw new ResourceNotFoundException("project does not require qualification with id: " + employee.getQualificationId());
            if (!employeeReadService.getRequest(employee.getEmployeeId()).getSkillSet().stream()
                    .map(x -> x.getId()).toList().contains(employee.getQualificationId())) throw new ResourceNotFoundException("employee does not own qualification with id: " + employee.getQualificationId());
            for (var project :
                    allProjects) {
                if (project.getEmployeeMemberships().stream().anyMatch(x -> x.getEmployeeId().equals(employee.getEmployeeId()))) {
                    if (project.getStartDate().before(projectEntity.getEndDate()) && projectEntity.getStartDate().before(project.getEndDate())) {
                        throw new PlanningConflictException("employee with id " + employee.getEmployeeId() + " already tied to another project with id " + project.getId());
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

    public List<ProjectEntity> getAllProjectsByEmployeeId(Long id) {
        return employeeMembershipRepository.findAllByEmployeeId(id).stream().map(x -> x.getProject()).toList();
    }

    public GetEmployeeDto findById(Long id) {
        return employeeReadService.getRequest(id);
    }

    public void ensureAddMemberToProjectRequestIsSafe(Long projectId, Long employeeId, Long qualificationId) {
        var projectResponse = projectRepository.findById(projectId);
        if (projectResponse.isEmpty()) throw new ResourceNotFoundException("project with id "+ projectId + " does not exist");
        var employeeResponse = employeeReadService.getRequest(employeeId);
        if (employeeResponse == null) throw new ResourceNotFoundException("employee with id "+ employeeId + " does not exist");

        var allProjects = projectRepository.findAll();
        if (!projectResponse.get().getQualificationConnections().stream().map(x-> x.getQualificationId()).toList().contains(qualificationId)) throw new ResourceNotFoundException("project does not require qualification with id: " + qualificationId);
        if (!employeeResponse.getSkillSet().stream()
                .map(x -> x.getId()).toList().contains(qualificationId)) throw new ResourceNotFoundException("employee does not own qualification with id: " + qualificationId);
        for (var project :
                allProjects) {
            if (project.getEmployeeMemberships().stream().anyMatch(x -> x.getEmployeeId().equals(employeeId))) {
                if (project.getStartDate().before(projectResponse.get().getEndDate()) && projectResponse.get().getStartDate().before(project.getEndDate())) {
                    throw new PlanningConflictException("employee with id " + employeeId + " already tied to another project with id " + project.getId());
                }
            }
        }
    }

    public ProjectEntity AddMemberToProject(Long projectId, Long employeeId, Long qualificationId) {
        var project = projectRepository.findById(projectId).get();
        var entity = new EmployeeMembershipEntity();
        entity.setEmployeeId(employeeId);
        entity.setQualificationId(qualificationId);
        entity.setProject(project);

        employeeMembershipRepository.save(entity);
        project.getEmployeeMemberships().add(entity);
        return project;
    }

    public void ensureRemoveMemberFromProjectRequestIsSafe(Long projectId, Long employeeId) {
        var projectResponse = projectRepository.findById(projectId);
        if (projectResponse.isEmpty()) throw new ResourceNotFoundException("project with id "+ projectId + " does not exist");
        if (projectRepository.findById(projectId).get().getEmployeeMemberships().stream().filter(x -> Objects.equals(x.getEmployeeId(), employeeId)).toArray().length != 1 ) throw new ResourceNotFoundException("no membership between project with id "+ projectId+ " and employee with id "+ employeeId+ " exists");
    }

    public void removeEmployeeFromProject(Long projectId, Long employeeId) {
        var project = projectRepository.findById(projectId).get();
        var membership = project.getEmployeeMemberships().stream().filter(x -> Objects.equals(x.getEmployeeId(), employeeId)).findFirst().get();
        project.getEmployeeMemberships().remove(membership);
        projectRepository.save(project);
    }
}
