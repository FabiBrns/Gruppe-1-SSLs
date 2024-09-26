package de.szut.lf8_starter.project.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class UpdateProjectDto {
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @NotEmpty
    private Date startDate;
    @NotNull
    @NotEmpty
    private Date endDate;
}
