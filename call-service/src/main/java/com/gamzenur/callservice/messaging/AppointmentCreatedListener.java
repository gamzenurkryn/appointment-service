package com.gamzenur.callservice.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.callservice.config.RabbitMqConfig;
import com.gamzenur.callservice.dto.CreateCallRequest;
import com.gamzenur.callservice.service.CallService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class AppointmentCreatedListener {

    private final ObjectMapper objectMapper;
    private final CallService callService;

    public AppointmentCreatedListener(ObjectMapper objectMapper, CallService callService) {
        this.objectMapper = objectMapper;
        this.callService = callService;
    }

    @RabbitListener(
            queues = RabbitMqConfig.APPOINTMENTS_QUEUE,
            containerFactory = "rawMessageListenerContainerFactory"
    )
    public void receive(Message message) throws IOException {
        JsonNode event = objectMapper.readTree(message.getBody());
        UUID appointmentId = UUID.fromString(event.path("appointment").path("id").asText());

        CreateCallRequest request = new CreateCallRequest();
        request.setAppointmentId(appointmentId);
        callService.createCall(request);
    }
}
