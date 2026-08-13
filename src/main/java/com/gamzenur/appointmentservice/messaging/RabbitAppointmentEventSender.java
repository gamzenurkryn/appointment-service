package com.gamzenur.appointmentservice.messaging;

import com.gamzenur.appointmentservice.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class RabbitAppointmentEventSender {

    private final RabbitTemplate rabbitTemplate;

    public RabbitAppointmentEventSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(AppointmentEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.APPOINTMENTS_EXCHANGE,
                event.eventType(),
                event
        );
    }
}
