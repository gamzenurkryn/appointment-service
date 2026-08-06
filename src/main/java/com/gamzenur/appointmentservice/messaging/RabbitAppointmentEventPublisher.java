package com.gamzenur.appointmentservice.messaging;

import com.gamzenur.appointmentservice.config.CorrelationIdFilter;
import com.gamzenur.appointmentservice.config.RabbitMqConfig;
import com.gamzenur.appointmentservice.dto.AppointmentResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class RabbitAppointmentEventPublisher implements AppointmentEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public RabbitAppointmentEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publishCreated(AppointmentResponse appointment) {
        publish(RabbitMqConfig.CREATED_ROUTING_KEY, appointment);
    }

    @Override
    public void publishUpdated(AppointmentResponse appointment) {
        publish(RabbitMqConfig.UPDATED_ROUTING_KEY, appointment);
    }

    @Override
    public void publishCancelled(AppointmentResponse appointment) {
        publish(RabbitMqConfig.CANCELLED_ROUTING_KEY, appointment);
    }

    private void publish(String eventType, AppointmentResponse appointment) {
        AppointmentEvent event = new AppointmentEvent(
                UUID.randomUUID(),
                eventType,
                OffsetDateTime.now(),
                currentCorrelationId(),
                appointment
        );
        applicationEventPublisher.publishEvent(event);
    }

    private String currentCorrelationId() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            Object correlationId = attributes.getRequest()
                    .getAttribute(CorrelationIdFilter.ATTRIBUTE_NAME);
            if (correlationId instanceof String value && !value.isBlank()) {
                return value;
            }
        }
        return UUID.randomUUID().toString();
    }
}
