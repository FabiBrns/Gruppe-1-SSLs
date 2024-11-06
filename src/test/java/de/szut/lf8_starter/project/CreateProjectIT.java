package de.szut.lf8_starter.project;

import de.szut.lf8_starter.project.dtos.AddProjectDto;
import de.szut.lf8_starter.project.employeeMembership.Dtos.AddEmployeeMembershipDto;
import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipEntity;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionForProjectDto;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class CreateProjectIT extends AbstractIntegrationTest {
    ProjectService projectService;

    @Test
    void authorization() throws Exception {
        this.mockMvc.perform(post("/project")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "user")
    void createProjectSuccessfully() throws Exception {
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

        ProjectEntity mockProject = new ProjectEntity();
        mockProject.setName("Project SSL");
        mockProject.setStartDate(Date.valueOf(LocalDate.of(2024, 11, 6)));
        mockProject.setEndDate(Date.valueOf(LocalDate.of(2024, 11, 7)));

        EmployeeMembershipEntity mockEmployee = new EmployeeMembershipEntity();
        mockEmployee.setEmployeeId(297L);
        mockEmployee.setQualificationId(207L);
        mockProject.setEmployeeMemberships(List.of(mockEmployee));

        QualificationConnectionEntity mockQualification = new QualificationConnectionEntity();
        mockQualification.setQualificationId(207L);
        mockQualification.setNeededEmployeesWithQualificationCount(1);
        mockProject.setQualificationConnections(List.of(mockQualification));

        when(projectService.createProject((AddProjectDto) any(AddProjectDto.class))).thenReturn(mockProject);

        mockMvc.perform(post("/project")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Project SSL")))
                .andExpect(jsonPath("$.startDate", is("2024-11-06T11:25:21.000+00:00")))
                .andExpect(jsonPath("$.endDate", is("2024-11-07T11:25:21.000+00:00")))
                .andExpect(jsonPath("$.employees", hasSize(1)))
                .andExpect(jsonPath("$.employees[0].employeeId", is(297)))
                .andExpect(jsonPath("$.employees[0].qualificationId", is(207)));
    }

    @Test
    @WithMockUser(roles = "user")
    void createProjectWithEndDateBeforeStartDate() throws Exception {
        String startDateStr = "2024-11-06T11:25:21.000+01:00";
        String endDateStr = "2024-11-01T11:25:21.000+01:00";

        this.mockMvc.perform(post("/project")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Invalid Project\",\"startDate\":\"" + startDateStr + "\",\"endDate\":\"" + endDateStr + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("end date shouldn't be before start date")));
    }
}
