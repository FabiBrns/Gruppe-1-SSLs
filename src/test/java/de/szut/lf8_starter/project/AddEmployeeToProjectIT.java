package de.szut.lf8_starter.project;

import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AddEmployeeToProjectIT extends AbstractIntegrationTest {

    @Test
    @WithMockUser(roles = "user")
    public void addEmployeeToProject_Authenticated() throws Exception {
        
    }

    @Test
    @WithMockUser(roles = "user")
    void addEmployeeToProject_Unauthorized() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 1, \"qualificationId\": 2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addEmployeeToProject_Unauthenticated() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/employees", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 1, \"qualificationId\": 2}"))
                .andExpect(status().isUnauthorized());
    }
}
