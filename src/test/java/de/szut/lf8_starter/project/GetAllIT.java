package de.szut.lf8_starter.project;

import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

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

    // TEST: ProjectId, ProjektBezeichnung und Liste der Qualifikationen sowie Start- und Enddatum

    @Test
    @WithMockUser(roles = "user")
    void findAll() throws Exception {
        var test1 = new ProjectEntity();
        test1.setName("Road to Grand Champion");
        projectRepository.save(test1);

        var test2 = new ProjectEntity();
        test2.setName("Downfall to Bronze");
        projectRepository.save(test2);

        final var contentAsString = this.mockMvc.perform(get("/project")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Road to Grand Champion")))
                .andExpect(jsonPath("$[1].name", is("Downfall to Bronze")));
    }

    @Test
    @WithMockUser(roles = "user")
    void findAllWhenEmpty() throws Exception {
        this.mockMvc.perform(get("/project")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
