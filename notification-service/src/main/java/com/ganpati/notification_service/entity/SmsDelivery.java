package com.ganpati.notification_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
public class SmsDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appointmentId;
    private String messageSid;
    private String phoneNumber;
    private String status;
    private String type;  // "CONFIRMATION" or "REMINDER"
    private String errorCode;
    private LocalDateTime updatedAt;

}