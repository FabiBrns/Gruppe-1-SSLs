package de.szut.lf8_starter.project.employeeMembership;

import de.szut.lf8_starter.project.qualificationConnection.QualificationConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface EmployeeMembershipRepository extends JpaRepository<EmployeeMembershipEntity, Long> {
    List<EmployeeMembershipEntity>  findAllByEmployeeId(Long id);
}
