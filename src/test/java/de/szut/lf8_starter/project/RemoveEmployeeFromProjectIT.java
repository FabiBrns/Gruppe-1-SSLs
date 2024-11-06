package de.szut.lf8_starter.project;

import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipRepository;
import de.szut.lf8_starter.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RemoveEmployeeFromProjectIT extends AbstractIntegrationTest {
    // TEST: Success code 200 bei erfolgreichem Löschen
}
