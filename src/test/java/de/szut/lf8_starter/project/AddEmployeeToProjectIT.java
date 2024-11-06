package de.szut.lf8_starter.project;

import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetEmployeeDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetQualificationDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.EmployeeReadService;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.QualificationReadService;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AddEmployeeToProjectIT extends AbstractIntegrationTest {

    @MockBean
    EmployeeReadService employeeReadService;

    @MockBean
    QualificationReadService qualificationReadService;

    private ProjectEntity project1;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        var qualificationDtoJava = new GetQualificationDto();
        qualificationDtoJava.setId(207L);
        qualificationDtoJava.setSkill("Java");

        var qualificationDtoDocker = new GetQualificationDto();
        qualificationDtoDocker.setId(208L);
        qualificationDtoDocker.setSkill("Docker");

        var employeeDto = new GetEmployeeDto();
        employeeDto.setId(1L);
        employeeDto.setFirstName("Krasser Typ");
        employeeDto.setSkillSet(List.of(qualificationDtoJava));

        when(employeeReadService.getRequest(1L)).thenReturn(employeeDto);
        when(qualificationReadService.getRequest(208L)).thenReturn(qualificationDtoDocker);

        var qualificationConnectionEntity = new QualificationConnectionEntity();
        qualificationConnectionEntity.setQualificationId(207L);
        qualificationConnectionEntity.setNeededEmployeesWithQualificationCount(1);

        var project = new ProjectEntity();
        project.setName("Epic Win Project");
        project.setStartDate(Date.valueOf("2024-11-06"));
        project.setPlannedEndDate(Date.valueOf("2024-11-07"));
        this.project1 = projectRepository.save(project);

        qualificationConnectionEntity.setProject(project);
        qualificationConnectionRepository.save(qualificationConnectionEntity);
    }

    @Test
    void authorization() throws Exception {
        this.mockMvc.perform(post("/project/1/employees")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "user")
    public void addEmployeeToProject_Success() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                        .with(csrf())
                        .param("projectId", String.valueOf(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 1, \"qualificationId\": 207}"))

                // then
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "user")
    public void addEmployeeToProject_EmployeeDoesNotExist() throws Exception {
        // when
        when(employeeReadService.getRequest(999L)).thenReturn(null);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                        .with(csrf())
                        .param("projectId", String.valueOf(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 999, \"qualificationId\": 207}"))

                // then
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "user")
    public void addEmployeeToProject_EmployeeDoesNotHaveRequiredQualification() throws Exception {
        // given
        var qualificationDto = new GetQualificationDto();
        qualificationDto.setId(208L);
        var employeeDto = new GetEmployeeDto();
        employeeDto.setSkillSet(List.of(qualificationDto));

        // when
        when(employeeReadService.getRequest(1L)).thenReturn(employeeDto);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                        .with(csrf())
                        .param("projectId", String.valueOf(project1.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 1, \"qualificationId\": 208}"))

                //then
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "user")
    public void addEmployeeToProject_EmployeeTimeConflict() throws Exception {
        // given
        var project = new ProjectEntity();
        project.setName("Epic Win Project 2");
        project.setStartDate(Date.valueOf("2024-11-06"));
        project.setPlannedEndDate(Date.valueOf("2024-11-07"));
        project = projectRepository.save(project);

        // when
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 2L)
                        .with(csrf())
                        .param("projectId", String.valueOf(project.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 1, \"qualificationId\": 207}"))

                // then
                .andExpect(status().isConflict());
    }
}
