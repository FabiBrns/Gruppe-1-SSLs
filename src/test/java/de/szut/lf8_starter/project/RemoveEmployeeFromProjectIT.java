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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RemoveEmployeeFromProjectIT extends AbstractIntegrationTest {

    @MockBean
    EmployeeReadService employeeReadService;

    private ProjectEntity project1;

    @Test
    void authorization() throws Exception {
        this.mockMvc.perform(delete("/project/1/employees")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        var qualificationDtoJava = new GetQualificationDto();
        qualificationDtoJava.setId(207L);
        qualificationDtoJava.setSkill("Java");

        var employeeDto = new GetEmployeeDto();
        employeeDto.setId(1L);
        employeeDto.setFirstName("Krasser Typ");
        employeeDto.setSkillSet(List.of(qualificationDtoJava));

        when(employeeReadService.getRequest(1L)).thenReturn(employeeDto);

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
    @WithMockUser(roles = "user")
    void removeEmployeeFromProject_Success() throws Exception {
        // given
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                .with(csrf())
                .param("projectId", String.valueOf(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\": 1, \"qualificationId\": 207}"));

        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/project/{projectId}/employees", project1.getId())
                .with(csrf())
                .param("projectId", String.valueOf(project1.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\": 1}"));

        // then
        assertThat(employeeMembershipRepository.findAllByEmployeeId(1L).isEmpty()).isTrue();
    }
}
