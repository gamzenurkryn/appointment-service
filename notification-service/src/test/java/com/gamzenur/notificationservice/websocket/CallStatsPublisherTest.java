package com.gamzenur.notificationservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CallStatsPublisherTest {

    @Test
    void shouldPublishActiveCallStats() throws Exception {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CallStatsPublisher publisher = new CallStatsPublisher(template, objectMapper);
        var event = objectMapper.readTree("""
                {
                  "correlationId": "stats-test",
                  "call": {
                    "id": "call-1",
                    "status": "ACTIVE",
                    "participantCount": 2
                  }
                }
                """);

        publisher.updateAndPublish(event);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSend(eq(RealtimeEventPublisher.EVENTS_TOPIC), captor.capture());
        RealtimeEvent published = (RealtimeEvent) captor.getValue();
        assertThat(published.event()).isEqualTo("stats.updated");
        assertThat(published.payload().path("activeCalls").asLong()).isEqualTo(1);
        assertThat(published.payload().path("participants").asLong()).isEqualTo(2);
    }
}
