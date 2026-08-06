package com.gamzenur.notificationservice.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.notificationservice.config.RabbitMqConfig;
import com.gamzenur.notificationservice.service.LogEventService;
import com.gamzenur.notificationservice.websocket.RealtimeEventPublisher;
import com.gamzenur.notificationservice.websocket.CallStatsPublisher;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class CentralLogListener {

    private final ObjectMapper objectMapper;
    private final LogEventService logEventService;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final CallStatsPublisher callStatsPublisher;

    public CentralLogListener(
            ObjectMapper objectMapper,
            LogEventService logEventService,
            RealtimeEventPublisher realtimeEventPublisher,
            CallStatsPublisher callStatsPublisher
    ) {
        this.objectMapper = objectMapper;
        this.logEventService = logEventService;
        this.realtimeEventPublisher = realtimeEventPublisher;
        this.callStatsPublisher = callStatsPublisher;
    }

    @RabbitListener(queues = {
            RabbitMqConfig.APPOINTMENTS_QUEUE,
            RabbitMqConfig.WHATSAPP_QUEUE,
            RabbitMqConfig.CALLS_QUEUE
    })
    public void receive(Message message) throws IOException {
        JsonNode payload = objectMapper.readTree(message.getBody());
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        logEventService.recordRabbitEvent(routingKey, payload);
        realtimeEventPublisher.publish(routingKey, payload);
        if (routingKey.startsWith("call.")) {
            callStatsPublisher.updateAndPublish(payload);
        }
    }
}
