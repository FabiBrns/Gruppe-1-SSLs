package de.szut.lf8_starter.project.employeeMembership;

import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetEmployeeDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetQualificationDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.EmployeeReadService;
import de.szut.lf8_starter.exceptionHandling.*;
import de.szut.lf8_starter.project.ProjectEntity;
import de.szut.lf8_starter.project.ProjectRepository;
import de.szut.lf8_starter.project.employeeMembership.Dtos.AddEmployeeMembershipDto;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionForProjectDto;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeMembershipService {
    private final EmployeeMembershipRepository employeeMembershipRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeReadService employeeReadService;

    public EmployeeMembershipService(ProjectRepository projectRepository,
                                     EmployeeMembershipRepository employeeMembershipRepository,
                                     EmployeeReadService employeeReadService) {
        this.projectRepository = projectRepository;
        this.employeeReadService = employeeReadService;
        this.employeeMembershipRepository = employeeMembershipRepository;
    }
    public void ensureUpdateDateOnProjectIsSafe(ProjectEntity entity, Date startDate, Date plannedEndDate, Date actualEndDate) {
        var existentEmployeeMemberships = entity.getEmployeeMemberships();

        for (var membership : existentEmployeeMemberships) {
            var otherProjectsForEmployee = employeeMembershipRepository.findAllByEmployeeId(membership.getEmployeeId())
                    .stream()
                    .filter(otherMembership -> !otherMembership.getProject().getId().equals(entity.getId()))
                    .toList();

            for (var potentiallyConflictingMembership : otherProjectsForEmployee) {
                var potentiallyConflictingProject = potentiallyConflictingMembership.getProject();
                var potentiallyConflictingStartDate = potentiallyConflictingProject.getStartDate();
                var potentiallyConflictingEndDate = potentiallyConflictingProject.getActualEndDate() != null ? potentiallyConflictingProject.getActualEndDate() : potentiallyConflictingProject.getPlannedEndDate();

                var endDateToUse = actualEndDate != null ? actualEndDate : plannedEndDate;

                if (startDate.before(potentiallyConflictingEndDate) && endDateToUse.after(potentiallyConflictingStartDate)) {
                    throw new PlanningConflictException(
                            "Employee with ID " + membership.getEmployeeId() +
                                    " is already tied to another project with ID " + potentiallyConflictingProject.getId()
                    );
                }
            }
        }
    }


    public void ensureAddAllMembersToProjectRequestIsSafe(ProjectEntity projectEntity, Set<AddEmployeeMembershipDto> employees, Set<AddQualificationConnectionForProjectDto> qualifications) {
        var allQualificationIds = new HashSet<>(qualifications.stream().map(AddQualificationConnectionForProjectDto::getQualificationId).toList());
        var allProjects = projectRepository.findAll();
        var existentQualificationCount = new HashMap<Long, Integer>();

        for (var employee : employees) {
            if (!allQualificationIds.contains(employee.getQualificationId())) {
                throw new QualificationConflictException("project does not require qualification with id: " + employee.getQualificationId());
            }
            if (employees.stream().filter(x -> x.getEmployeeId().equals(employee.getEmployeeId())).count() > 1) {
                throw new EmployeeConflictException("employee with id " + employee.getEmployeeId() + " present more than once in the list of employees");
            }
            if (!employeeReadService.getRequest(employee.getEmployeeId()).getSkillSet().stream().map(GetQualificationDto::getId)
                    .toList()
                    .contains(employee.getQualificationId())) {
                throw new EmployeeConflictException("employee does not own qualification with id: " + employee.getQualificationId());
            }
            for (var project : allProjects) {
                var isEmployeeInProject = project.getEmployeeMemberships().stream()
                        .anyMatch(membership -> membership.getEmployeeId().equals(employee.getEmployeeId()));

                if (isEmployeeInProject) {
                    var projectEndDate = project.getActualEndDate() != null ? project.getActualEndDate() : project.getPlannedEndDate();
                    var projectEntityEndDate = projectEntity.getActualEndDate() != null ? projectEntity.getActualEndDate() : projectEntity.getPlannedEndDate();

                    if (project.getStartDate().before(projectEntityEndDate) && projectEndDate.after(projectEntity.getStartDate())) {
                        throw new PlanningConflictException(
                                "Employee with ID " + employee.getEmployeeId() +
                                        " is already tied to another project with ID " + project.getId()
                        );
                    }
                }
            }

            existentQualificationCount.put(employee.getQualificationId(), existentQualificationCount.containsKey(employee.getQualificationId())
                    ? existentQualificationCount.get(employee.getQualificationId()) + 1
                    : 1);
        }
        for (var qualification : qualifications) {
            if (!existentQualificationCount.containsKey(qualification.getQualificationId())) continue;
            if (qualification.getNeededEmployeeCount() < existentQualificationCount.get(qualification.getQualificationId())) {
                throw new QualificationConflictException("Too many employees are assigned to qualification with id " + qualification.getQualificationId());
            }
        }
    }

    public void addAllEmployeesToProject(ProjectEntity projectEntity, Set<AddEmployeeMembershipDto> employees, Set<AddQualificationConnectionForProjectDto> qualifications) {
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
        return employeeMembershipRepository.findAllByEmployeeId(id).stream().map(EmployeeMembershipEntity::getProject).toList();
    }

    public GetEmployeeDto findById(Long id) {
        return employeeReadService.getRequest(id);
    }

    public void ensureAddMemberToProjectRequestIsSafe(Long projectId, Long employeeId, Long qualificationId) {
        var projectResponse = projectRepository.findById(projectId);
        if (projectResponse.isEmpty()) {
            throw new ResourceNotFoundException("project with id " + projectId + " does not exist");
        }
        var employeeResponse = employeeReadService.getRequest(employeeId);
        if (employeeResponse == null) {
            throw new ResourceNotFoundException("employee with id " + employeeId + " does not exist");
        }
        var allProjects = projectRepository.findAll();
        if (!projectResponse.get().getQualificationConnections().stream().map(QualificationConnectionEntity::getQualificationId)
                .toList()
                .contains(qualificationId)) {
            throw new QualificationConflictException("project does not require qualification with id: " + qualificationId);
        }
        if (!employeeResponse.getSkillSet().stream()
                .map(GetQualificationDto::getId)
                .toList()
                .contains(qualificationId)) {
            throw new EmployeeConflictException("employee does not own qualification with id: " + qualificationId);
        }
        if (projectResponse.get().getEmployeeMemberships().stream().anyMatch(x -> x.getEmployeeId().equals(employeeId))) {
            throw new EmployeeConflictException("employee already in project");
        }
        if (projectResponse.get().getEmployeeMemberships().stream().filter(x -> x.getQualificationId().equals(qualificationId)).count() >= projectResponse.get().getQualificationConnections().stream().filter(x -> x.getQualificationId().equals(qualificationId)).findFirst().get().getNeededEmployeesWithQualificationCount()) throw new QualificationConflictException("too many employees with specific qualification added"); // we are deeply sorry...
        Date projectResponseStartDate = projectResponse.get().getStartDate();
        Date projectResponseEndDate = projectResponse.get().getActualEndDate() != null
                ? projectResponse.get().getActualEndDate()
                : projectResponse.get().getPlannedEndDate();

        for (var project : allProjects) {
            var employeeAssignedToProject = project.getEmployeeMemberships().stream()
                    .anyMatch(membership -> membership.getEmployeeId().equals(employeeId));

            if (employeeAssignedToProject) {
                Date projectStartDate = project.getStartDate();
                Date projectEndDate = project.getActualEndDate() != null
                        ? project.getActualEndDate()
                        : project.getPlannedEndDate();

                if (projectStartDate.before(projectResponseEndDate) && projectResponseStartDate.before(projectEndDate)) {
                    throw new PlanningConflictException(
                            "Employee with ID " + employeeId + " is already tied to another project with ID " + project.getId()
                    );
                }
            }
        }

    }

    public ProjectEntity addMemberToProject(Long projectId, Long employeeId, Long qualificationId) {
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
        if (projectResponse.isEmpty()) {
            throw new ResourceNotFoundException("project with id " + projectId + " does not exist");
        }
        if (projectRepository.findById(projectId).get().getEmployeeMemberships().stream().filter(x -> Objects.equals(x.getEmployeeId(), employeeId)).toArray().length != 1) {
            throw new ResourceNotFoundException("no membership between project with id " + projectId + " and employee with id " + employeeId + " exists");
        }
    }

    public void removeEmployeeFromProject(Long projectId, Long employeeId) {
        var project = projectRepository.findById(projectId).get();
        var membership = project.getEmployeeMemberships().stream().filter(x -> Objects.equals(x.getEmployeeId(), employeeId))
                .findFirst()
                .get();
        project.getEmployeeMemberships().remove(membership);
        projectRepository.save(project);
    }
}
