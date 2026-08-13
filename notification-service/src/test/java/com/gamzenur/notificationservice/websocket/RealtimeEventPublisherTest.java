package com.gamzenur.notificationservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RealtimeEventPublisherTest {

    @Test
    void shouldPublishRabbitEventToEventsTopic() throws Exception {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        RealtimeEventPublisher publisher = new RealtimeEventPublisher(template);
        var payload = new ObjectMapper().readTree("""
                {
                  "eventType": "appointment.created",
                  "correlationId": "websocket-test-001"
                }
                """);

        publisher.publish("appointment.created", payload);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSend(eq(RealtimeEventPublisher.EVENTS_TOPIC), eventCaptor.capture());
        RealtimeEvent event = (RealtimeEvent) eventCaptor.getValue();
        assertThat(event.event()).isEqualTo("appointment.created");
        assertThat(event.correlationId()).isEqualTo("websocket-test-001");
        assertThat(event.payload()).isEqualTo(payload);
    }
}
