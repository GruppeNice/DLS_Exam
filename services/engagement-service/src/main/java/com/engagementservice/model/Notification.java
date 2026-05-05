package com.engagementservice.model;

import com.engagementservice.types.NotificationStatus;
import com.engagementservice.types.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String recipient;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
