package de.szut.lf8_starter.project.qualificationConnection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QualificationConnectionRepository extends JpaRepository<QualificationConnectionEntity, Long> {
    List<QualificationConnectionEntity> findAllByQualificationIdAndProjectId(Long qualificationId, Long projectId);
}
