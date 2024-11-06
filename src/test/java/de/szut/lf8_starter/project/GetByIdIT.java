package de.szut.lf8_starter.project;

import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetByIdIT extends AbstractIntegrationTest {

    @Test
    void authorization() throws Exception {
        this.mockMvc.perform(get("/project/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "user")
    void findById() throws Exception {
        // given
        var stored = projectRepository.save(new ProjectEntity());

        // when
        this.mockMvc.perform(get("/project/" + stored.getId())
                        .param("id", stored.getId().toString())
                        .with(csrf()))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stored.getId()));
    }

    @Test
    @WithMockUser(roles = "user")
    void findById_notFound() throws Exception {
        // given
        var stored = new ProjectEntity();
        stored.setId(999L);
        projectRepository.save(stored);

        // when
        this.mockMvc.perform(get("/project/" + stored.getId())
                        .param("id", String.valueOf(stored.getId()))
                        .with(csrf()))

                // then
                .andExpect(status().isNotFound());
    }
}