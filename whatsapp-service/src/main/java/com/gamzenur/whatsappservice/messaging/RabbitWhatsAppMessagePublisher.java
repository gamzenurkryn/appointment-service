package com.gamzenur.whatsappservice.messaging;

import com.gamzenur.whatsappservice.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class RabbitWhatsAppMessagePublisher implements WhatsAppMessagePublisher {
    private final RabbitTemplate rabbitTemplate;
    public RabbitWhatsAppMessagePublisher(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }
    @Override public void publish(MessageReceivedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.WHATSAPP_EXCHANGE,
                RabbitMqConfig.MESSAGE_RECEIVED_ROUTING_KEY, event);
    }
}
