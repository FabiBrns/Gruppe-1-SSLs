package de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GetEmployeeDto {
    private Long id;
    private String lastName;
    private String firstName;
    private List<GetQualificationDto> skillSet;
}
