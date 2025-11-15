package com.ganpati.notification_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganpati.base_domain.dto.AppointmentDto;
import com.ganpati.notification_service.client.AppointmentClient;
import com.ganpati.notification_service.config.AppConstants;
import com.ganpati.notification_service.config.TwilioConfig;
import com.ganpati.notification_service.dto.SmsResponseDto;
import com.ganpati.notification_service.dto.SmsStatus;
import com.ganpati.notification_service.entity.SmsDelivery;
import com.ganpati.notification_service.repository.SmsDeliveryRepository;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsService.class);

    private TwilioConfig twilioConfig;
    private ObjectMapper mapper;
    private AppointmentClient appointmentClient;
    private SmsDeliveryRepository smsDeliveryRepository;

    public SmsService(TwilioConfig twilioConfig, ObjectMapper mapper, AppointmentClient appointmentClient, SmsDeliveryRepository smsDeliveryRepository) {
        this.twilioConfig = twilioConfig;
        this.mapper = mapper;
        this.appointmentClient = appointmentClient;
        this.smsDeliveryRepository = smsDeliveryRepository;
    }

    @KafkaListener(topics = AppConstants.TOPIC,groupId = AppConstants.Group_ID)
    public SmsResponseDto sendAppointmentBookingSms(String appointmentEvent) throws JsonProcessingException {
        LOGGER.info("appointment received -> {} ", appointmentEvent);

        //converting String to AppointmentDto Object. Because we are accepting kay and value for kafkaTemplate<String,String> are String.
        AppointmentDto appointmentDto = mapper.readValue(appointmentEvent, AppointmentDto.class);
        SmsResponseDto smsResponseDto = null;

        try{
            PhoneNumber to = new PhoneNumber(appointmentDto.getCustomerPhone());
            PhoneNumber from = new PhoneNumber(twilioConfig.getPhoneNumber());
            LocalDateTime appointmentTime = appointmentDto.getAppointmentTime();
            String date = appointmentTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
            String time = appointmentTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));

            String appointmentMessage = "Your appointment is confirmed.\n" +
                    "Date: " + date + "\n" +
                    "Time: " + time + "\n" +
                    "Thank you.";

            Message message = Message
                    .creator(to, from,
                            appointmentMessage)
                    .setStatusCallback(twilioConfig.getStatusCallbackUrl())//status callback url to update status in db
                    .create();

            // Save record in DB:
            SmsDelivery delivery = new SmsDelivery();
            delivery.setAppointmentId(appointmentDto.getId());
            delivery.setMessageSid(message.getSid());
            delivery.setPhoneNumber(appointmentDto.getCustomerPhone());
            delivery.setStatus("queued");
            delivery.setType("CONFIRMATION");
            smsDeliveryRepository.save(delivery);

            smsResponseDto = new SmsResponseDto(SmsStatus.DELIVERED,appointmentMessage);
        }catch (Exception e){
            e.printStackTrace();
            smsResponseDto = new SmsResponseDto(SmsStatus.FAILED,e.getMessage());
        }
        return smsResponseDto;
    }

    @Scheduled(cron = "0 * * * * *") // every minute
    public void sendReminders() {
        LOGGER.info("~~~~~~~~~~~~~~~~~~~~~~~~");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusHours(1).minusMinutes(1);
        LocalDateTime to = now.plusHours(1).plusMinutes(1);

        List<AppointmentDto> appointments = appointmentClient.findUpcomingAppointments(from, to);

        for (AppointmentDto appointment : appointments) {
            createReminderMessage(appointment);
            //further we can publish event to appointment-service instead of this feignClint call
            //We moved below logic in controller class in handleStatusCallback() that will execute when twilio will update message status in db.
            //appointmentClient.markReminderSent(appointment.getId()); // ✅ update status remotely
        }
    }

    public void createReminderMessage(AppointmentDto appointment){
        PhoneNumber toPhone = new PhoneNumber(appointment.getCustomerPhone());
        PhoneNumber fromPhone = new PhoneNumber(twilioConfig.getPhoneNumber());
        LocalDateTime appointmentTime = appointment.getAppointmentTime();
        String date = appointmentTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        String time = appointmentTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));

        String reminderMessage = "Reminder: Your appointment is scheduled on "
                + date + " at " + time + ".";

        Message message = Message
                .creator(toPhone, fromPhone,
                        reminderMessage)
                .setStatusCallback(twilioConfig.getStatusCallbackUrl())//status callback url to update status in db
                .create();

        // Save record in DB:
        // || NOTE- If you are using unverified numbers then twilio will throw exception in above line. so below code will not execute.
        // If you want to see negative case i.e failed status in db then you need to use varified number only but switch off the phone.
        SmsDelivery delivery = new SmsDelivery();
        delivery.setAppointmentId(appointment.getId());
        delivery.setMessageSid(message.getSid());
        delivery.setPhoneNumber(appointment.getCustomerPhone());
        delivery.setStatus("queued");
        delivery.setType("REMINDER");//this going to be used for markReminderSent =true in appointment table. check controller class for logic
        smsDeliveryRepository.save(delivery);
    }
}
