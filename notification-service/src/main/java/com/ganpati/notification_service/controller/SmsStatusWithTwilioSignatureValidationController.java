//package com.ganpati.notification_service.controller;
//
//import com.ganpati.notification_service.client.AppointmentClient;
//import com.ganpati.notification_service.config.TwilioConfig;
//import com.ganpati.notification_service.repository.SmsDeliveryRepository;
//import com.twilio.security.RequestValidator;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.Map;
///*
//* Twilio sends a special header with every webhook: X-Twilio-Signature
//* This signature proves the request really came from Twilio.
//* Even if someone knows your URL, they cannot fake Twilio’s signature
//* */
//
//@RestController
//@RequestMapping("/api/sms")
//public class SmsStatusWithTwilioSignatureValidationController {
//
//    //NOTE THIS CONTROLLER ONLY FOR EXTRA AUTHENTICATION SO THAT ONLY TWILIO CAN ACCESS THIS ENDPOINT WITH X-Twilio-Signature
//    //YOU NEED TO COMMENT ONE OF THE CONTROLLER BECAUSE BOTH HAVE SAME ENDPOINTS
//    private final SmsDeliveryRepository smsDeliveryRepository;
//    private final AppointmentClient appointmentClient;
//    private final TwilioConfig twilioConfig;
//
//
//    public SmsStatusWithTwilioSignatureValidationController(SmsDeliveryRepository smsDeliveryRepository,
//                                                            AppointmentClient appointmentClient, TwilioConfig twilioConfig) {
//        this.smsDeliveryRepository = smsDeliveryRepository;
//        this.appointmentClient = appointmentClient;
//        this.twilioConfig = twilioConfig;
//    }
//
//    @PostMapping("/statusCallback")
//    public ResponseEntity<Void> handleStatusCallback(
//            @RequestHeader(value = "X-Twilio-Signature", required = false) String twilioSignature,
//            @RequestParam Map<String,String> form,
//            HttpServletRequest request) {
//
//        // Step 1: Validate signature
//        if (!isValidTwilioRequest(twilioSignature, form, request)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        // Step 2: Extract values
//        String sid = form.get("MessageSid");
//        String status = form.get("MessageStatus");
//        String errorCode = form.get("ErrorCode");
//
//        // Step 3: Update DB
//        smsDeliveryRepository.findByMessageSid(sid).ifPresent(delivery -> {
//            delivery.setStatus(status);
//            delivery.setErrorCode(errorCode);
//            delivery.setUpdatedAt(LocalDateTime.now());
//            smsDeliveryRepository.save(delivery);
//
//            if ("delivered".equalsIgnoreCase(status)) {
//                if ("REMINDER".equalsIgnoreCase(delivery.getType())) {
//                    appointmentClient.markReminderSent(delivery.getAppointmentId());
//                }
//            }
//        });
//
//        return ResponseEntity.ok().build();
//    }
//
//    private boolean isValidTwilioRequest(String signature,
//                                         Map<String,String> form,
//                                         HttpServletRequest request) {
//
//        if (signature == null) return false;
//
//        RequestValidator validator = new RequestValidator(twilioConfig.getAuthToken());
//
//        String url = request.getRequestURL().toString();
//
//        return validator.validate(url, form, signature);
//    }
//}
//
