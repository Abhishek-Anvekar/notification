package com.ganpati.notification_service.client;

import com.ganpati.base_domain.dto.AppointmentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(url = "http://localhost:8080/api/appointments", value = "appointment-service")
public interface AppointmentClient {

    @GetMapping("/upcoming")
    public List<AppointmentDto> findUpcomingAppointments(
            @RequestParam("from") LocalDateTime from,
            @RequestParam("to") LocalDateTime to);

    @PutMapping("/{id}/reminder-sent")
    public void markReminderSent(@PathVariable Long id);
}
