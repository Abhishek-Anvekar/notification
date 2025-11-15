package com.ganpati.notification_service.controller;

import com.ganpati.notification_service.client.AppointmentClient;
import com.ganpati.notification_service.repository.SmsDeliveryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
public class SmsStatusController {

    private final SmsDeliveryRepository smsDeliveryRepository;
    private final AppointmentClient appointmentClient;

    public SmsStatusController(SmsDeliveryRepository smsDeliveryRepository,
                               AppointmentClient appointmentClient) {
        this.smsDeliveryRepository = smsDeliveryRepository;
        this.appointmentClient = appointmentClient;
    }

    @PostMapping("/statusCallback")
    public ResponseEntity<Void> handleStatusCallback(@RequestParam Map<String,String> form) {
        String sid = form.get("MessageSid");
        String status = form.get("MessageStatus");
        String errorCode = form.get("ErrorCode");

        // find record
        smsDeliveryRepository.findByMessageSid(sid).ifPresent(delivery -> {
            delivery.setStatus(status);
            delivery.setErrorCode(errorCode);
            delivery.setUpdatedAt(LocalDateTime.now());
            smsDeliveryRepository.save(delivery);

            if ("delivered".equalsIgnoreCase(status)) {
                if ("REMINDER".equalsIgnoreCase(delivery.getType())) {
                    // appointment reminder delivered — update appointment service
                    appointmentClient.markReminderSent(delivery.getAppointmentId());
                }
            }
        });

        return ResponseEntity.ok().build();
    }
}
