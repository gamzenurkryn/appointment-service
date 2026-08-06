package com.gamzenur.appointmentservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.appointmentservice.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CallCompletedListenerTest {

    @Test
    void appliesCompletedCallResultToAppointment() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        String json = """
                {
                  "eventType": "call.ended",
                  "call": {
                    "appointmentId": "%s",
                    "status": "COMPLETED",
                    "result": "CONFIRMED"
                  }
                }
                """.formatted(appointmentId);
        AppointmentService appointmentService = mock(AppointmentService.class);
        CallCompletedListener listener = new CallCompletedListener(new ObjectMapper(), appointmentService);

        listener.handle(new Message(json.getBytes(StandardCharsets.UTF_8)));

        verify(appointmentService).applyCompletedCallResult(appointmentId, "CONFIRMED");
    }
}
