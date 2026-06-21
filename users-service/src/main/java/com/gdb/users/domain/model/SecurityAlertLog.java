package com.gdb.users.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * TODO: MOD4-CR-01: Alert Tracking Entity.
 * Trainee task: Map this class as a JPA Entity using @Entity, @Id, @GeneratedValue, and @Column.
 */
@Entity
@Table(name = "security_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlertLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String message;

    @Column(name = "alert_date")
    private LocalDateTime alertDate;
}
