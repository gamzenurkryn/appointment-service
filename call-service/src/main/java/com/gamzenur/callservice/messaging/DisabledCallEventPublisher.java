package com.gamzenur.callservice.messaging;

import com.gamzenur.callservice.dto.CallResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "integration.rabbitmq.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class DisabledCallEventPublisher implements CallEventPublisher {

    @Override
    public void publish(String eventType, CallResponse call) {
        // RabbitMQ kapalı geliştirme ortamında olay yayını yapılmaz.
    }
}
