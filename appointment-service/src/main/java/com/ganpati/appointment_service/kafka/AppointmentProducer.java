package com.ganpati.appointment_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganpati.appointment_service.dto.AppointmentDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AppointmentProducer {

    private KafkaTemplate<String, String> kafkaTemplate;
    private NewTopic topic;
    private ObjectMapper mapper;

    public AppointmentProducer(KafkaTemplate<String, String> kafkaTemplate, NewTopic topic, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.mapper = mapper;
    }

    public void send(AppointmentDto appointmentDto) throws JsonProcessingException {
        kafkaTemplate.send(topic.name(),mapper.writeValueAsString(appointmentDto));
    }
}
