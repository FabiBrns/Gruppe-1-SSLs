package de.szut.lf8_starter.project;

import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetEmployeeDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetQualificationDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.EmployeeReadService;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AddEmployeeToProjectIT extends AbstractIntegrationTest {

    @MockBean
    EmployeeReadService employeeReadService;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        var qualificationDto = new GetQualificationDto();
        qualificationDto.setId(207L);
        qualificationDto.setSkill("Java");

        var employeeDto = new GetEmployeeDto();
        employeeDto.setId(1L);
        employeeDto.setFirstName("Krasser Typ");
        employeeDto.setSkillSet(List.of(qualificationDto));

        when(employeeReadService.getRequest(1L)).thenReturn(employeeDto);

        var qualificationConnectionEntity = new QualificationConnectionEntity();
        qualificationConnectionEntity.setQualificationId(207L);
        qualificationConnectionEntity.setNeededEmployeesWithQualificationCount(1);

        var project = new ProjectEntity();
        project.setName("Epic Win Project");
        project.setStartDate(Date.valueOf("2024-11-06"));
        project.setPlannedEndDate(Date.valueOf("2024-11-07"));
        projectRepository.save(project);


        qualificationConnectionEntity.setProject(project);
        qualificationConnectionRepository.save(qualificationConnectionEntity);
    }

    @Test
    @WithMockUser(roles = "user")
    public void addEmployeeToProject_Success() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                        .with(csrf())
                        .param("projectId", String.valueOf(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 1, \"qualificationId\": 207}"))
                .andExpect(status().isCreated());
    }

    // TEST: Existiert Mitarbeiter?

    @Test
    @WithMockUser(roles = "user")
    public void addEmployeeToProject_EmployeeDoesNotExist() throws Exception {
        when(employeeReadService.getRequest(999L)).thenReturn(null);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                        .with(csrf())
                        .param("projectId", String.valueOf(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 999, \"qualificationId\": 207}"))
                .andExpect(status().isNotFound());
    }

    // TEST: Hat Mitarbeiter Qualifikation?

    @Test
    @WithMockUser(roles = "user")
    public void addEmployeeToProject_EmployeeDoesNotHaveRequiredQualification() throws Exception {
        var qualificationDto = new GetQualificationDto();
        qualificationDto.setId(208L);
        var employeeDto = new GetEmployeeDto();
        employeeDto.setSkillSet(List.of(qualificationDto));

        when(employeeReadService.getRequest(1L)).thenReturn(employeeDto);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                        .with(csrf())
                        .param("projectId", String.valueOf(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 1, \"qualificationId\": 208}"))
                .andExpect(status().isConflict());
    }

    // FEHLT NOCH
    // TEST: Time conflict beim mitarbeiter

    @Test
    @WithMockUser(roles = "user")
    void addEmployeeToProject_Unauthorized() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("projectId", String.valueOf(1))
                        .content("{\"employeeId\": 1, \"qualificationId\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addEmployeeToProject_Unauthenticated() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                        .with(csrf())
                        .param("projectId", String.valueOf(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 1, \"qualificationId\": 1}"))
                .andExpect(status().isUnauthorized());
    }
}
