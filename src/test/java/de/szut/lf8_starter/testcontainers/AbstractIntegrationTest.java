package de.szut.lf8_starter.testcontainers;

import de.szut.lf8_starter.project.ProjectRepository;
import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipRepository;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
@ContextConfiguration(initializers = PostgresContextInitializer.class)
public class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected QualificationConnectionRepository qualificationConnectionRepository;

    @Autowired
    protected EmployeeMembershipRepository employeeMembershipRepository;

    @BeforeEach
    public void setUp() {
        projectRepository.deleteAll();
        qualificationConnectionRepository.deleteAll();
        employeeMembershipRepository.deleteAll();

        System.out.print("\n\nSUPER\n\n");

    }
}
