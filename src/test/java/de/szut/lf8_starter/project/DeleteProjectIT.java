package de.szut.lf8_starter.project;

import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

public class DeleteProjectIT extends AbstractIntegrationTest {

    @Test
    void authorization() throws Exception {
        this.mockMvc.perform(delete("/project/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "user")
    void happyPath() throws Exception {
        // when
        var stored = projectRepository.save(new ProjectEntity());

        this.mockMvc.perform(delete("/project/1")
                        .param("id", String.valueOf(stored.getId()))
                        .with(csrf()))

                // then
                .andExpect(status().isOk());
        assertThat(projectRepository.findById(stored.getId()).isPresent()).isFalse();
    }

    @Test
    @WithMockUser(roles = "user")
    void deleteWithInvalidIdFormat() throws Exception {
        // when
        this.mockMvc.perform(delete("/project/invalid-id")
                        .with(csrf()))

                // then
                .andExpect(status().isBadRequest());
    }
}