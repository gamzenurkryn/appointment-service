package com.gamzenur.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.notificationservice.domain.LogEvent;
import com.gamzenur.notificationservice.repository.LogEventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogEventServiceTest {

    @Test
    void shouldRecordAppointmentEventWithoutCustomerDataInMessage() throws Exception {
        LogEventRepository repository = mock(LogEventRepository.class);
        LogEventService service = new LogEventService(repository, new ObjectMapper());

        service.recordRabbitEvent(
                "appointment.created",
                new ObjectMapper().readTree("""
                        {
                          "occurredAt": "2026-08-04T12:00:00Z",
                          "correlationId": "test-correlation"
                        }
                        """)
        );

        ArgumentCaptor<LogEvent> captor = ArgumentCaptor.forClass(LogEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getService()).isEqualTo("appointment-service");
        assertThat(captor.getValue().getCorrelationId()).isEqualTo("test-correlation");
        assertThat(captor.getValue().getMessage()).doesNotContain("customer");
    }

    @Test
    void shouldReadNumericRabbitTimestamp() throws Exception {
        LogEventRepository repository = mock(LogEventRepository.class);
        LogEventService service = new LogEventService(repository, new ObjectMapper());

        service.recordRabbitEvent(
                "appointment.created",
                new ObjectMapper().readTree("""
                        {
                          "occurredAt": 1785848350.8775218,
                          "correlationId": "numeric-time-test"
                        }
                        """)
        );

        ArgumentCaptor<LogEvent> captor = ArgumentCaptor.forClass(LogEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTimestamp().getYear()).isEqualTo(2026);
    }
}
