package com.gamzenur.callservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.callservice.dto.CreateCallRequest;
import com.gamzenur.callservice.service.CallService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AppointmentCreatedListenerTest {

    @Test
    void startsCallForCreatedAppointmentEvent() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        CallService callService = mock(CallService.class);
        AppointmentCreatedListener listener = new AppointmentCreatedListener(
                new ObjectMapper(),
                callService
        );
        String json = """
                {
                  "eventType": "appointment.created",
                  "appointment": { "id": "%s" }
                }
                """.formatted(appointmentId);

        listener.receive(new Message(json.getBytes(StandardCharsets.UTF_8)));

        ArgumentCaptor<CreateCallRequest> captor = ArgumentCaptor.forClass(CreateCallRequest.class);
        verify(callService).createCall(captor.capture());
        assertThat(captor.getValue().getAppointmentId()).isEqualTo(appointmentId);
    }
}
