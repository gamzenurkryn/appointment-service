package com.gamzenur.callservice.messaging;

import com.gamzenur.callservice.dto.CallResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class RabbitCallEventPublisher implements CallEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public RabbitCallEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(String eventType, CallResponse call) {
        applicationEventPublisher.publishEvent(new CallEvent(
                UUID.randomUUID(),
                eventType,
                OffsetDateTime.now(),
                currentCorrelationId(),
                call
        ));
    }

    private String currentCorrelationId() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String value = attributes.getRequest().getHeader("X-Correlation-Id");
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return UUID.randomUUID().toString();
    }
}
