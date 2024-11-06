package de.szut.lf8_starter.project;

import de.szut.lf8_starter.exceptionHandling.EmployeeConflictException;
import de.szut.lf8_starter.exceptionHandling.ResourceNotFoundException;
import de.szut.lf8_starter.project.dtos.AddProjectDto;
import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipEntity;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CreateProjectIT extends AbstractIntegrationTest {

    @MockBean
    private ProjectService projectService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authorization() throws Exception {
        this.mockMvc.perform(post("/project")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "user")
    void createProjectSuccessfully() throws Exception {
        // given
        final String content = """
                {
                    "name": "Project SSL",
                    "startDate": "2024-11-06T11:25:21.000+00:00",
                    "endDate": "2024-11-07T11:25:21.000+00:00",
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
        when(projectService.createProject(any(AddProjectDto.class))).thenReturn(mockedResponse);

        mockMvc.perform(post("/project")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))

                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Project SSL")))
                .andExpect(jsonPath("$.startDate", is("2024-11-06")))
                .andExpect(jsonPath("$.plannedEndDate", is("2024-11-07")))
                .andExpect(jsonPath("$.employees", hasSize(1)))
                .andExpect(jsonPath("$.employees[0].employeeId", is(297)))
                .andExpect(jsonPath("$.employees[0].qualificationId", is(207)));

        verify(projectService, times(1)).createProject(any(AddProjectDto.class));
    }

    @Test
    @WithMockUser(roles = "user")
    void createProjectWithMissingEmployeeOrQualification() throws Exception {
        // given
        final String content = """
                {
                    "name": "Project SSL",
                    "startDate": "2024-11-06T11:25:21.000+00:00",
                    "endDate": "2024-11-07T11:25:21.000+00:00",
                    "employees": [],
                    "qualifications": []
                }
                """;

        // when
        when(projectService.createProject(any(AddProjectDto.class)))
                .thenThrow(new ResourceNotFoundException("Employee or qualification not found"));

        mockMvc.perform(post("/project")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))

                // then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Employee or qualification not found")));

        verify(projectService, times(1)).createProject(any(AddProjectDto.class));
    }

    @Test
    @WithMockUser(roles = "user")
    void createProjectWithEmployeeTimeConflict() throws Exception {
        // given
        final String content = """
                {
                    "name": "Project SSL",
                    "startDate": "2024-11-06T11:25:21.000+00:00",
                    "endDate": "2024-11-10T11:25:21.000+00:00",
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

        // when
        when(projectService.createProject(any(AddProjectDto.class)))
                .thenThrow(new EmployeeConflictException("Employee 297 has a scheduling conflict"));

        mockMvc.perform(post("/project")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))

                // then
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Employee 297 has a scheduling conflict")));

        verify(projectService, times(1)).createProject(any(AddProjectDto.class));
    }
}