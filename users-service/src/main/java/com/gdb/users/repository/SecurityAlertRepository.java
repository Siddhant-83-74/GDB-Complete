package com.gdb.users.repository;

import com.gdb.users.domain.model.SecurityAlertLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO: MOD4-CR-01: JpaRepository interface.
 * Trainee task: Declare this as a Spring Data JPA Repository interface.
 */
@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlertLog, Long> {
}
