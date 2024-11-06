package de.szut.lf8_starter.project;

import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetEmployeeDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetQualificationDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.EmployeeReadService;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.QualificationReadService;
import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipEntity;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetEmployeeByIdIT extends AbstractIntegrationTest {

    @MockBean
    EmployeeReadService employeeReadService;

    @MockBean
    QualificationReadService qualificationReadService;

    private ProjectEntity project1;
    private ProjectEntity project2;
    private EmployeeMembershipEntity employeeMembership;

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

        var employeeMembership1 = new EmployeeMembershipEntity();
        employeeMembership1.setEmployeeId(297L);
        employeeMembership1.setProject(project1);

        var employeeMembership2 = new EmployeeMembershipEntity();
        employeeMembership2.setEmployeeId(297L);
        employeeMembership2.setProject(project2);

        var projectA = new ProjectEntity();
        projectA.setName("Epic Win Project");
        projectA.setStartDate(Date.valueOf("2024-11-06"));
        projectA.setPlannedEndDate(Date.valueOf("2024-11-07"));
        projectA.setEmployeeMemberships(List.of(employeeMembership1));

        var projectB = new ProjectEntity();
        projectB.setName("Epic Win Project 2");
        projectB.setStartDate(Date.valueOf("2024-09-06"));
        projectB.setPlannedEndDate(Date.valueOf("2024-09-07"));
        projectB.setEmployeeMemberships(List.of(employeeMembership2));

        this.project1 = projectRepository.save(projectA);
        this.project2 = projectRepository.save(projectB);

        qualificationConnectionEntity.setProject(projectB);
        qualificationConnectionRepository.save(qualificationConnectionEntity);
    }


    @Test
    void authorization() throws Exception {
        this.mockMvc.perform(get("/project/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "user")
    void getProjectsEmployeeById() throws Exception {
        // given
        var qualificationDtoJava = new GetQualificationDto();
        qualificationDtoJava.setId(207L);
        qualificationDtoJava.setSkill("Java");

        var mockedEmployeeDto = new GetEmployeeDto();
        mockedEmployeeDto.setId(1L);
        mockedEmployeeDto.setFirstName("Krasser Typ");
        mockedEmployeeDto.setSkillSet(List.of(qualificationDtoJava));

        //when
        when(employeeReadService.getRequest(1L)).thenReturn(mockedEmployeeDto);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/project/employee/{id}", mockedEmployeeDto.getId())
                        .with(csrf())
                        .param("id", String.valueOf(mockedEmployeeDto.getId()))
                        .contentType(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "user")
    void getProjectsForNonExistentEmployee() throws Exception {
        //given
        long nonExistentEmployeeId = 999L;

        //when
        when(employeeReadService.getRequest(nonExistentEmployeeId)).thenReturn(null);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/project/employee/{id}", nonExistentEmployeeId)
                        .with(csrf())
                        .param("id", String.valueOf(nonExistentEmployeeId))
                        .contentType(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isNotFound());
    }

}