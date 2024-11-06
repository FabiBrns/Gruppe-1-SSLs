package de.szut.lf8_starter.project.dtos;

import de.szut.lf8_starter.project.employeeMembership.Dtos.GetEmployeeMembershipDto;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.GetQualificationConnectionDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class GetProjectDto {
    private Long id;
    private String name;
    private Date startDate;
    private Date plannedEndDate;
    private Date actualEndDate;
    private int customerId;
    private String contactPersonName;
    private String comment;
    private Set<GetQualificationConnectionDto> qualifications;
    private Set<GetEmployeeMembershipDto> employees;
}
