package de.szut.lf8_starter.project.dtos;

import de.szut.lf8_starter.project.employeeMembership.Dtos.AddEmployeeMembershipDto;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionForProjectDto;
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
    private Date plannedEndDate;
    @NotNull
    private int customerId;
    @NotNull
    @NotEmpty
    private String contactPersonName;
    @NotNull
    @NotEmpty
    private String comment;

    private Set<AddEmployeeMembershipDto> employees;
    private Set<AddQualificationConnectionForProjectDto> qualifications;
}
