package de.szut.lf8_starter.project.employeeMembership;

import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetEmployeeDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetQualificationDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.EmployeeReadService;
import de.szut.lf8_starter.exceptionHandling.EmployeeConflictException;
import de.szut.lf8_starter.exceptionHandling.PlanningConflictException;
import de.szut.lf8_starter.exceptionHandling.QualificationConflictException;
import de.szut.lf8_starter.exceptionHandling.ResourceNotFoundException;
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

    public void ensureUpdateDateOnProjectIsSafe(ProjectEntity entity, Date startDate, Date endDate) {
        var existentEmployeeMembershipsIds = entity.getEmployeeMemberships();
        for (var membership :
                existentEmployeeMembershipsIds) {
            var allMembershipsForEmployee = employeeMembershipRepository.findAllByEmployeeId(membership.getEmployeeId()).stream()
                    .filter(x -> !x.getProject().getId().equals(entity.getId()))
                    .toList();
            for (var membershipToCheckForConflictsWith :
                    allMembershipsForEmployee) {
                if (membershipToCheckForConflictsWith.getProject().getStartDate().before(startDate) && endDate.before(membershipToCheckForConflictsWith.getProject().getEndDate())) {
                    throw new PlanningConflictException("employee with id " + membershipToCheckForConflictsWith.getEmployeeId() + " already tied to another project with id " + membershipToCheckForConflictsWith.getProject().getId());
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
                if (project.getEmployeeMemberships().stream().anyMatch(x -> x.getEmployeeId().equals(employee.getEmployeeId()))) {
                    if (project.getStartDate().before(projectEntity.getEndDate()) && projectEntity.getStartDate().before(project.getEndDate())) {
                        throw new PlanningConflictException("employee with id " + employee.getEmployeeId() + " already tied to another project with id " + project.getId());
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
        // we are deeply sorry...
        if (projectResponse.get().getEmployeeMemberships().stream().filter(x -> x.getQualificationId().equals(qualificationId))
                    .count() >= projectResponse
                    .get()
                    .getQualificationConnections()
                    .stream().filter(x -> x.getQualificationId()
                        .equals(qualificationId))
                    .findFirst()
                    .get()
                    .getNeededEmployeesWithQualificationCount()
        ) {
        throw new QualificationConflictException("too many employees with specific qualification added");
        }
        for (var project : allProjects) {
            if (project.getEmployeeMemberships().stream().anyMatch(x -> x.getEmployeeId().equals(employeeId))) {
                if (project.getStartDate().before(projectResponse.get().getEndDate()) && projectResponse.get().getStartDate().before(project.getEndDate())) {
                    throw new PlanningConflictException("employee with id " + employeeId + " already tied to another project with id " + project.getId());
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
