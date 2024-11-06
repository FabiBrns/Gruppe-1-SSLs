package de.szut.lf8_starter.project;

import de.szut.lf8_starter.project.employeeMembership.EmployeeMembershipEntity;
import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private Date startDate;
    private Date plannedEndDate;
    private Date actualEndDate;
    private int customerId;
    private String contactPersonName;
    private String comment;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "project",
            orphanRemoval = true)
    private List<EmployeeMembershipEntity> employeeMemberships;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "project",
            orphanRemoval = true)
    private List<QualificationConnectionEntity> qualificationConnections;
}
