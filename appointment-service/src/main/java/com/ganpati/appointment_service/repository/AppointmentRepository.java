package com.ganpati.appointment_service.repository;

import com.ganpati.appointment_service.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.reminderSent = false AND a.appointmentTime BETWEEN :from AND :to")
    List<Appointment> findUpcomingAppointments(@Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);
}