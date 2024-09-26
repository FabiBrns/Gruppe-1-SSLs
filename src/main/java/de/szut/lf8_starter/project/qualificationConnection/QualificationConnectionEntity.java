package de.szut.lf8_starter.project.qualificationConnection;

import de.szut.lf8_starter.project.ProjectEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class QualificationConnectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long qualificationId;
    @ManyToOne(cascade = CascadeType.ALL)
    private ProjectEntity project;
}
