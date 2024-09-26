package de.szut.lf8_starter.project.dtos;

import de.szut.lf8_starter.project.employeeMembership.Dtos.AddEmployeeMembershipDto;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class AddProjectDto {
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @NotEmpty
    private Date startDate;
    @NotNull
    @NotEmpty
    private Date endDate;
    private Set<AddEmployeeMembershipDto> employees;
    private Set<AddQualificationConnectionDto> qualifications;
}
