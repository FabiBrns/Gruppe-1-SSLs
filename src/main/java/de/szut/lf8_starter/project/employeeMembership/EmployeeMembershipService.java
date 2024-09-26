package de.szut.lf8_starter.project.employeeMembership;

import de.szut.lf8_starter.EmployeeWebServiceAccessPoint.EmployeeReadService;
import de.szut.lf8_starter.project.ProjectEntity;
import de.szut.lf8_starter.project.ProjectRepository;
import de.szut.lf8_starter.project.employeeMembership.Dtos.AddEmployeeMembershipDto;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionDto;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    public boolean CanAddAllMembersToProject(ProjectEntity projectEntity, Set<AddEmployeeMembershipDto> employees, Set<AddQualificationConnectionDto> qualifications) {
        var allQualificationIds = new HashSet(qualifications.stream().map(AddQualificationConnectionDto::getQualificationId).toList());
        var allProjects = projectRepository.findAll();
        for (var employee:
             employees) {
            if (!allQualificationIds.contains(employee.getQualificationId())) return false;
            if (!employeeReadService.GetRequest(employee.getEmployeeId()).getSkillSet().stream()
                    .map(x -> x.getId()).toList().contains(employee.getQualificationId())) return false;
            for (var project :
                    allProjects) {
                if (project.getEmployeeMemberships().stream().anyMatch(x -> x.getEmployeeId().equals(employee.getEmployeeId()))) {
                    if (project.getStartDate().before(projectEntity.getEndDate()) && projectEntity.getStartDate().before(project.getEndDate())) {
                        return false;
                    }
                }
            }
        }
        return true;
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
