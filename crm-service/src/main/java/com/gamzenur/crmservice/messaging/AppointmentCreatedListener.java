package com.gamzenur.crmservice.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.crmservice.config.RabbitMqConfig;
import com.gamzenur.crmservice.service.OfferService;
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
    private final OfferService offerService;

    public AppointmentCreatedListener(ObjectMapper objectMapper, OfferService offerService) {
        this.objectMapper = objectMapper;
        this.offerService = offerService;
    }

    @RabbitListener(queues = RabbitMqConfig.APPOINTMENTS_QUEUE)
    public void onAppointmentCreated(Message message) throws IOException {
        JsonNode event = objectMapper.readTree(message.getBody());
        JsonNode appointment = event.path("appointment");
        offerService.createForAppointment(
                UUID.fromString(appointment.path("id").asText()),
                UUID.fromString(appointment.path("storeId").asText()),
                appointment.path("serviceType").asText(),
                event.path("correlationId").asText()
        );
    }
}
