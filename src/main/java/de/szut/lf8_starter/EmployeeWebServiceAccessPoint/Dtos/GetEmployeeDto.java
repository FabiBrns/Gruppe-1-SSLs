package de.szut.lf8_starter.EmployeeWebServiceAccessPoint.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GetEmployeeDto {
    private Long id;
    private String lastName;
    private String firstName;
    private List<GetQualificationDto> skillSet;
}
