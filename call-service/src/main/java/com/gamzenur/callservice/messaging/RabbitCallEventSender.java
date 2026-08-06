package com.gamzenur.callservice.messaging;

import com.gamzenur.callservice.config.RabbitMqConfig;
import com.gamzenur.callservice.domain.CallStatus;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class RabbitCallEventSender {

    private final RabbitTemplate rabbitTemplate;

    public RabbitCallEventSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(CallEvent event) {
        String routingKey = event.call().getStatus() == CallStatus.COMPLETED
                ? RabbitMqConfig.CALL_COMPLETED_ROUTING_KEY
                : event.eventType();
        rabbitTemplate.convertAndSend(RabbitMqConfig.CALLS_EXCHANGE, routingKey, event);
    }
}
