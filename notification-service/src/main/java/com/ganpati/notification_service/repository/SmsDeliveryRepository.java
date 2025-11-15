package com.ganpati.notification_service.repository;

import com.ganpati.notification_service.entity.SmsDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsDeliveryRepository extends JpaRepository<SmsDelivery,Long> {
    Optional<SmsDelivery> findByMessageSid(String sid);
}
