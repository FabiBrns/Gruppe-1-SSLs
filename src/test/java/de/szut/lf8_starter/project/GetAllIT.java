package de.szut.lf8_starter.project;

import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetQualificationDto;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.sql.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class GetAllIT extends AbstractIntegrationTest {

    @Test
    void authorization() throws Exception {
        this.mockMvc.perform(get("/project")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "user")
    void findAllWithProjectDetails() throws Exception {
        // given
        var test1 = new ProjectEntity();
        test1.setName("Road to Grand Champion");
        test1.setStartDate(Date.valueOf("2024-11-06"));
        test1.setPlannedEndDate(Date.valueOf("2024-11-07"));
        projectRepository.save(test1);

        var test2 = new ProjectEntity();
        test2.setName("Downfall to Bronze");
        test2.setStartDate(Date.valueOf("2024-12-01"));
        test2.setPlannedEndDate(Date.valueOf("2024-12-02"));
        projectRepository.save(test2);

        var qualificationDtoJava = new GetQualificationDto();
        qualificationDtoJava.setId(207L);
        qualificationDtoJava.setSkill("Java");

        var qualificationDtoDocker = new GetQualificationDto();
        qualificationDtoDocker.setId(208L);
        qualificationDtoDocker.setSkill("Docker");

        var qualificationConnectionEntity1 = new QualificationConnectionEntity();
        qualificationConnectionEntity1.setQualificationId(207L);
        qualificationConnectionEntity1.setNeededEmployeesWithQualificationCount(1);

        var qualificationConnectionEntity2 = new QualificationConnectionEntity();
        qualificationConnectionEntity2.setQualificationId(208L);
        qualificationConnectionEntity2.setNeededEmployeesWithQualificationCount(1);

        test1.setQualificationConnections(List.of(qualificationConnectionEntity1));
        test2.setQualificationConnections(List.of(qualificationConnectionEntity2));

        // when
        this.mockMvc.perform(get("/project")
                        .with(csrf()))

                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Road to Grand Champion")))
                .andExpect(jsonPath("$[1].name", is("Downfall to Bronze")))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[1].id", notNullValue()))
                .andExpect(jsonPath("$[0].startDate", is("2024-11-05T23:00:00.000+00:00")))
                .andExpect(jsonPath("$[0].plannedEndDate", is("2024-11-06T23:00:00.000+00:00")))
                .andExpect(jsonPath("$[1].startDate", is("2024-11-30T23:00:00.000+00:00")))
                .andExpect(jsonPath("$[1].plannedEndDate", is("2024-12-01T23:00:00.000+00:00")));
    }

    @Test
    @WithMockUser(roles = "user")
    void findAll() throws Exception {
        // given
        var test1 = new ProjectEntity();
        test1.setName("Road to Grand Champion");
        projectRepository.save(test1);

        var test2 = new ProjectEntity();
        test2.setName("Downfall to Bronze");
        projectRepository.save(test2);

        // when
        this.mockMvc.perform(get("/project")
                        .with(csrf()))

                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Road to Grand Champion")))
                .andExpect(jsonPath("$[1].name", is("Downfall to Bronze")));
    }

    @Test
    @WithMockUser(roles = "user")
    void findAllWhenEmpty() throws Exception {
        // when
        this.mockMvc.perform(get("/project")
                        .with(csrf()))

                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
