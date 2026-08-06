package com.gamzenur.appointmentservice.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.appointmentservice.config.RabbitMqConfig;
import com.gamzenur.appointmentservice.service.AppointmentService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class CallCompletedListener {

    private final ObjectMapper objectMapper;
    private final AppointmentService appointmentService;

    public CallCompletedListener(ObjectMapper objectMapper, AppointmentService appointmentService) {
        this.objectMapper = objectMapper;
        this.appointmentService = appointmentService;
    }

    @RabbitListener(
            queues = RabbitMqConfig.APPOINTMENT_CALLS_QUEUE,
            containerFactory = "rawMessageListenerContainerFactory"
    )
    public void handle(Message message) throws IOException {
        JsonNode call = objectMapper.readTree(message.getBody()).path("call");
        String appointmentId = call.path("appointmentId").asText();
        String result = call.path("result").asText();
        if (appointmentId.isBlank() || result.isBlank()) {
            return;
        }
        appointmentService.applyCompletedCallResult(UUID.fromString(appointmentId), result);
    }
}
