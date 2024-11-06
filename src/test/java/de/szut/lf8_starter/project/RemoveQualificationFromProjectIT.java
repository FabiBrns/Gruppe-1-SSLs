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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RemoveQualificationFromProjectIT extends AbstractIntegrationTest {

    @MockBean
    QualificationConnectionRepository qualificationConnectionRepository;

    private ProjectEntity project1;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        var qualificationDtoJava = new GetQualificationDto();
        qualificationDtoJava.setId(207L);
        qualificationDtoJava.setSkill("Java");

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
        this.mockMvc.perform(delete("/project/1/qualifications")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "user")
    void removeQualificationFromProject_Success() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/project/{projectId}/qualifications", project1.getId())
                        .with(csrf())
                        .param("projectId", project1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 207}"))
                .andExpect(status().isOk());

        assertThat(qualificationConnectionRepository.
                findAllByQualificationIdAndProjectId(207L, project1.getId()).isEmpty()).isTrue();
    }

    @Test
    @WithMockUser(roles = "user")
    void removeQualificationFromProject_QualificationNotFound() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/project/{projectId}/qualifications", project1.getId())
                        .with(csrf())
                        .param("projectId", project1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}
