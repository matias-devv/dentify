package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity @Getter  @Setter @AllArgsConstructor
@NoArgsConstructor @Table ( name = "notifications")
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_notification;

    @ManyToOne ( fetch = FetchType.LAZY)
    @JoinColumn ( name = "id_appointment")
    private Appointment appointment;

    @OneToOne ( mappedBy = "notification")
    private Pay pay;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    @Enumerated(EnumType.STRING)
    private NotificationChannel notificationChannel;
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    private String message;
    private LocalDate date_generation;
    private LocalDate date_sent;
    private Integer maximum_attempts;
    private Integer attempts_made;
    private String error_message;

}
