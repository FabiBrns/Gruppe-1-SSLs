package de.szut.lf8_starter.project;

import de.szut.lf8_starter.project.dtos.UpdateProjectDto;
import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipEntity;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.sql.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateProjectIT extends AbstractIntegrationTest {

    @MockBean
    ProjectService projectService;

    @Test
    void authorization() throws Exception {
        this.mockMvc.perform(put("/project")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "user")
    void updateProject_Success() throws Exception {
        // given
        final String content = """
                {
                    "name": "Project SSL",
                    "startDate": "2024-11-06T11:25:21.000+00:00",
                    "plannedEndDate": "2024-11-07T11:25:21.000+00:00",
                    "actualEndDate": "2024-11-08T11:25:21.000+00:00",
                    "customerId": 123,
                    "contactPersonName": "Max Mustermann",
                    "employees": [
                          {
                            "employeeId": 297,
                            "qualificationId": 207
                          }
                    ],
                    "qualifications": [
                          {
                            "qualificationId": 207,
                            "neededEmployeeCount": 1
                          }
                    ]
                }
                """;

        UpdateProjectDto updateProjectDto = new UpdateProjectDto();
        updateProjectDto.setName("Project SSL");
        updateProjectDto.setStartDate(Date.valueOf("2024-11-06"));
        updateProjectDto.setPlannedEndDate(Date.valueOf("2024-11-07"));
        updateProjectDto.setActualEndDate(Date.valueOf("2024-11-08"));
        updateProjectDto.setCustomerId(123);
        updateProjectDto.setContactPersonName("Max Mustermann");

        var mockedResponse = new ProjectEntity();
        mockedResponse.setName("Project SSL");
        mockedResponse.setStartDate(Date.valueOf("2024-11-06"));
        mockedResponse.setPlannedEndDate(Date.valueOf("2024-11-07"));

        var employee = new EmployeeMembershipEntity();
        employee.setEmployeeId(297L);
        employee.setQualificationId(207L);
        mockedResponse.setEmployeeMemberships(List.of(employee));
        mockedResponse.setQualificationConnections(List.of(new QualificationConnectionEntity()));

        // when
        when(projectService.updateById(anyLong(), any(UpdateProjectDto.class))).thenReturn(mockedResponse);

        mockMvc.perform(put("/project/{id}", 1L)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .param("id", "1"))

                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Project SSL")))
                .andExpect(jsonPath("$.startDate", is("2024-11-06")))
                .andExpect(jsonPath("$.plannedEndDate", is("2024-11-07")))
                .andExpect(jsonPath("$.employees", hasSize(1)))
                .andExpect(jsonPath("$.employees[0].employeeId", is(297)))
                .andExpect(jsonPath("$.employees[0].qualificationId", is(207)));

        verify(projectService, times(1)).updateById(anyLong(), any(UpdateProjectDto.class));
    }
}
