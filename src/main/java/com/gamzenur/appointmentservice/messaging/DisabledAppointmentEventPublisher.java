package com.gamzenur.appointmentservice.messaging;

import com.gamzenur.appointmentservice.dto.AppointmentResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "integration.rabbitmq.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class DisabledAppointmentEventPublisher implements AppointmentEventPublisher {

    @Override
    public void publishCreated(AppointmentResponse appointment) {
    }

    @Override
    public void publishUpdated(AppointmentResponse appointment) {
    }

    @Override
    public void publishCancelled(AppointmentResponse appointment) {
    }
}
