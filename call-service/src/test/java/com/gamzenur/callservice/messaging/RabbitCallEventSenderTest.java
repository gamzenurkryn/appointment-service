package com.gamzenur.callservice.messaging;

import com.gamzenur.callservice.config.RabbitMqConfig;
import com.gamzenur.callservice.domain.CallStatus;
import com.gamzenur.callservice.dto.CallResponse;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitCallEventSenderTest {

    @Test
    void routesCompletedCallWithDocumentedRoutingKey() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitCallEventSender sender = new RabbitCallEventSender(rabbitTemplate);
        CallResponse call = new CallResponse();
        call.setStatus(CallStatus.COMPLETED);
        CallEvent event = new CallEvent(
                UUID.randomUUID(),
                "call.ended",
                OffsetDateTime.now(),
                UUID.randomUUID().toString(),
                call
        );

        sender.send(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMqConfig.CALLS_EXCHANGE,
                RabbitMqConfig.CALL_COMPLETED_ROUTING_KEY,
                event
        );
    }
}
