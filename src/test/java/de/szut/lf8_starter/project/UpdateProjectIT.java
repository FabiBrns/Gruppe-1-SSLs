package de.szut.lf8_starter.project;

import de.szut.lf8_starter.project.dtos.UpdateProjectDto;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateProjectIT extends AbstractIntegrationTest {

    @Test
    @WithMockUser(roles = "user")
    void updateProject_Success() throws Exception {
        // success 200 + check for ID, Bez, Zeitrahmen
    }


    @Test
    @WithMockUser(roles = "user")
    void updateProject_Unauthorized() throws Exception {

    }

    @Test
    void updateProject_Unauthenticated() throws Exception {

    }

    @Test
    void updateProject_NoEmployeeConflicts() throws Exception {
        // 200 bei ok, sonst 409 + EmployeeId
    }

    @Test
    @WithMockUser(roles = "user")
    void updateProject_NotFound() throws Exception {
        UpdateProjectDto updateProjectDto = new UpdateProjectDto();
        updateProjectDto.setName("New Project Name");
        updateProjectDto.setStartDate(new Date());
        updateProjectDto.setEndDate(new Date(System.currentTimeMillis() + 864000000));

        this.mockMvc.perform(MockMvcRequestBuilders.put("/project/{id}", 999L)
                        .with(csrf())
                        .param("id", String.valueOf(999L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Project Name\", \"startDate\": \"" + new SimpleDateFormat("yyyy-MM-dd").format(updateProjectDto.getStartDate()) + "\", \"endDate\": \"" + new SimpleDateFormat("yyyy-MM-dd").format(updateProjectDto.getEndDate()) + "\"}"))
                .andExpect(status().isNotFound());
    }
}
