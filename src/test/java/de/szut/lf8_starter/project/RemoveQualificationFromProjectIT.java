package de.szut.lf8_starter.project;

import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionIndividualDto;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.RemoveQualificationConnectionDto;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RemoveQualificationFromProjectIT extends AbstractIntegrationTest {
    @Test
    @WithMockUser(roles = "user")
    void removeQualificationFromProject_Success() throws Exception {
        var project = new ProjectEntity();
        project.setName("Epic Win Project Number Two");
        project = projectRepository.save(project);

        var addDto = new AddQualificationConnectionIndividualDto();
        addDto.setQualificationId(1L);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/qualifications", project.getId())
                        .with(csrf())
                        .param("projectId", project.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 1}"))
                .andExpect(status().isCreated());

        var removeDto = new RemoveQualificationConnectionDto();
        removeDto.setQualificationId(1L);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/project/{projectId}/qualifications", project.getId())
                        .with(csrf())
                        .param("projectId", String.valueOf(project.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "user")
    void removeQualificationFromProject_Unauthorized() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/project/qualifications")
                        .param("projectId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeQualificationFromProject_Unauthenticated() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/project/qualifications")
                        .with(csrf())
                        .param("projectId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qualificationId\": 1}"))
                .andExpect(status().isUnauthorized());
    }
}
