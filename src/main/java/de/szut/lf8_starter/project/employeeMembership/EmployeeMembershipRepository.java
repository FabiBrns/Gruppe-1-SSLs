package de.szut.lf8_starter.project.employeeMembership;

import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeMembershipRepository extends JpaRepository<EmployeeMembershipEntity, Long> {
}
