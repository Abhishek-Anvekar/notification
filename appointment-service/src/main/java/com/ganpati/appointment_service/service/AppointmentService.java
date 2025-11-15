package com.ganpati.appointment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ganpati.appointment_service.dto.AppointmentDto;
import com.ganpati.appointment_service.entity.Appointment;
import com.ganpati.appointment_service.kafka.AppointmentProducer;
import com.ganpati.appointment_service.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private AppointmentProducer appointmentProducer;

    public AppointmentService(AppointmentRepository appointmentRepository, AppointmentProducer appointmentProducer) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentProducer = appointmentProducer;
    }

    @Transactional
    public AppointmentDto createAppointment(AppointmentDto dto) throws JsonProcessingException {
        Appointment appointment = new Appointment();
        appointment.setPatientId(dto.getPatientId());
        appointment.setDoctorId(dto.getDoctorId());
        appointment.setAppointmentTime(dto.getAppointmentTime());
        appointment.setCustomerPhone(dto.getCustomerPhone());
        appointment.setReminderSent(false);
        Appointment saved = appointmentRepository.save(appointment);
        dto.setId(saved.getId());

        appointmentProducer.send(dto);

        return dto;
    }

    public List<AppointmentDto> findUpcomingAppointments(LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.findUpcomingAppointments(from, to)
                .stream()
                .map(a -> {
                    AppointmentDto dto = new AppointmentDto();
                    dto.setId(a.getId());
                    dto.setPatientId(a.getPatientId());
                    dto.setDoctorId(a.getDoctorId());
                    dto.setAppointmentTime(a.getAppointmentTime());
                    dto.setCustomerPhone(a.getCustomerPhone());
                    dto.setReminderSent(a.isReminderSent());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void markReminderSent(Long appointmentId) {
        appointmentRepository.findById(appointmentId).ifPresent(a -> {
            a.setReminderSent(true);
            appointmentRepository.save(a);
        });
    }
}
