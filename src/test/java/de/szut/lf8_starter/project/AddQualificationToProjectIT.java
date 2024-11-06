package de.szut.lf8_starter.project;

import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetQualificationDto;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionRepository;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Date;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AddQualificationToProjectIT extends AbstractIntegrationTest {

    @MockBean
    private QualificationConnectionRepository qualificationConnectionRepository;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        var qualificationDto = new GetQualificationDto();
        qualificationDto.setId(207L);
        qualificationDto.setSkill("Java");

        var qualificationConnectionEntity = new QualificationConnectionEntity();
        qualificationConnectionEntity.setQualificationId(207L);
        qualificationConnectionEntity.setNeededEmployeesWithQualificationCount(1);

        var project = new ProjectEntity();
        project.setId(1L);
        project.setName("Epic Win Project");
        project.setStartDate(Date.valueOf("2024-11-06"));
        project.setPlannedEndDate(Date.valueOf("2024-11-07"));
        projectRepository.save(project);

        qualificationConnectionEntity.setProject(project);
        qualificationConnectionRepository.save(qualificationConnectionEntity);
    }

    @Test
    void authorization() throws Exception {
        this.mockMvc.perform(post("/project/1/qualifications")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "user")
    void addQualificationToProject_Success() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/qualifications", 1)
                        .with(csrf())
                        .param("projectId", String.valueOf(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 207}"))

                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Epic Win Project")))
                .andExpect(jsonPath("$.qualifications[0].qualificationId", is(207)));
    }

    @Test
    @WithMockUser(roles = "user")
    void addQualificationToProject_NotExistingQualificationId() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/qualifications", 1)
                        .with(csrf())
                        .param("projectId", String.valueOf(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 999}"))

                // then
                .andExpect(status().isNotFound());
    }
}
