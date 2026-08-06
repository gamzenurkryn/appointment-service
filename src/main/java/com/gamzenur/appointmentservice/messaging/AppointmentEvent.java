package com.gamzenur.appointmentservice.messaging;

import com.gamzenur.appointmentservice.dto.AppointmentResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentEvent(
        UUID eventId,
        String eventType,
        OffsetDateTime occurredAt,
        String correlationId,
        AppointmentResponse appointment
) {
}
