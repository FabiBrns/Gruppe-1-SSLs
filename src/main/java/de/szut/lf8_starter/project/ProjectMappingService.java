package de.szut.lf8_starter.project;

import de.szut.lf8_starter.project.dtos.GetProjectDto;
import de.szut.lf8_starter.project.employeeMembership.Dtos.GetEmployeeMembershipDto;
import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipEntity;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.GetQualificationConnectionDto;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ProjectMappingService {

    public GetProjectDto mapProjectEntityToGetProjectDto(ProjectEntity entity) {
        var dto = new GetProjectDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmployees(entity.getEmployeeMemberships().stream().map(this::mapEmployeeMembershipEntityToGetEmployeeMembershipDto).collect(Collectors.toSet()));
        dto.setQualifications(entity.getQualificationConnections().stream().map(this::mapQualificationConnectionEntityToGetQualificationConnectionDto).collect(Collectors.toSet()));
        dto.setEndDate(entity.getEndDate());
        dto.setStartDate(entity.getStartDate());
        return dto;
    }

    public GetEmployeeMembershipDto mapEmployeeMembershipEntityToGetEmployeeMembershipDto(EmployeeMembershipEntity entity) {
        var dto = new GetEmployeeMembershipDto();
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setQualificationId(entity.getQualificationId());
        return dto;
    }

    public GetQualificationConnectionDto mapQualificationConnectionEntityToGetQualificationConnectionDto(QualificationConnectionEntity entity) {
        var dto = new GetQualificationConnectionDto();
        dto.setQualificationId(entity.getQualificationId());
        return dto;
    }
}
