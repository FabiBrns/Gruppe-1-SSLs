package de.szut.lf8_starter.EmployeeWebServiceAccessPoint.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetAccessTokenDto {
    private String access_token;
    private int expires_in;
}
