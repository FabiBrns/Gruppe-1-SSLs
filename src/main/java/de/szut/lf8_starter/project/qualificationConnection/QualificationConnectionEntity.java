package de.szut.lf8_starter.project.qualificationConnection;

import de.szut.lf8_starter.project.ProjectEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class QualificationConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int qualificationId;
    @ManyToOne(cascade = CascadeType.ALL)
    private ProjectEntity project;
}
