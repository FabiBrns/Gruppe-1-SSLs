package de.szut.lf8_starter.project;

import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionIndividualDto;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AddQualificationToProjectIT extends AbstractIntegrationTest {
    @Test
    @WithMockUser(roles = "user")
    void addQualificationToProject_Success() throws Exception {
        var project = new ProjectEntity();
        project.setName("Epic Win Project");
        projectRepository.save(project);

        var qualificationDto = new AddQualificationConnectionIndividualDto();
        qualificationDto.setQualificationId(1L);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/qualifications", project.getId())
                        .with(csrf())
                        .param("projectId", String.valueOf(project.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Epic Win Project")))
                .andExpect(jsonPath("$.qualifications[0].qualificationId", is(1)));
    }

    @Test
    @WithMockUser(roles = "user")
    void addQualificationToProject_Unauthorized() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/qualifications", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addQualificationToProject_Unauthenticated() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/qualifications", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 1}"))
                .andExpect(status().isUnauthorized());
    }
}
