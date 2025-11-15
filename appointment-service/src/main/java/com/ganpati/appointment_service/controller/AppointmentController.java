package com.ganpati.appointment_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ganpati.appointment_service.dto.AppointmentDto;
import com.ganpati.appointment_service.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public AppointmentDto createAppointment(@RequestBody AppointmentDto dto) throws JsonProcessingException {
        return appointmentService.createAppointment(dto);
    }

    @GetMapping("/upcoming")
    public List<AppointmentDto> findUpcomingAppointments(
            @RequestParam("from") LocalDateTime from,
            @RequestParam("to") LocalDateTime to) {
        return appointmentService.findUpcomingAppointments(from, to);
    }

    @PutMapping("/{id}/reminder-sent")
    public void markReminderSent(@PathVariable Long id) {
        appointmentService.markReminderSent(id);
    }
}