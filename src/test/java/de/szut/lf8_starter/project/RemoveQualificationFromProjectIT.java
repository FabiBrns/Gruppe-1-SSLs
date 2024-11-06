package de.szut.lf8_starter.project;

import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetQualificationDto;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionRepository;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RemoveQualificationFromProjectIT extends AbstractIntegrationTest {

    @MockBean
    QualificationConnectionRepository qualificationConnectionRepository;

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
        project.setEndDate(Date.valueOf("2024-11-07"));
        projectRepository.save(project);

        qualificationConnectionEntity.setProject(project);
        qualificationConnectionRepository.save(qualificationConnectionEntity);
    }

    /* TODO:
    *   - test fail because of repetition! */

    @Test
    @WithMockUser(roles = "user")
    void removeQualificationFromProject_Success() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/project/{projectId}/qualifications", 1)
                        .with(csrf())
                        .param("projectId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 207}"))
                .andExpect(status().isOk());

        assertThat(qualificationConnectionRepository.
                findAllByQualificationIdAndProjectId(207L, 1L).isEmpty()).isTrue();
    }

    // TEST: 404 bei fehlender QualiId

    @Test
    @WithMockUser(roles = "notUser")
    void removeQualificationFromProject_Unauthorized() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/project/qualifications")
                        .with(csrf())
                        .param("projectId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 207}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeQualificationFromProject_Unauthenticated() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/project/qualifications")
                        .with(csrf())
                        .param("projectId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 207}"))
                .andExpect(status().isUnauthorized());
    }
}
