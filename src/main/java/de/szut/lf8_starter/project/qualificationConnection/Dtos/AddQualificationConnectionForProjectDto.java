package de.szut.lf8_starter.project.qualificationConnection.Dtos;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddQualificationConnectionForProjectDto {
    private Long qualificationId;
    @Min(1)
    private int neededEmployeeCount;
}