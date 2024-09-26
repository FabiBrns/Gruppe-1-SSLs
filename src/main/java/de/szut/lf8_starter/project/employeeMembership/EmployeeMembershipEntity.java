package de.szut.lf8_starter.project.employeeMembership;

import de.szut.lf8_starter.project.ProjectEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class EmployeeMembershipEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long employeeId;
    private Long qualificationId;
    @ManyToOne(cascade = CascadeType.DETACH)
    private ProjectEntity project;
}
