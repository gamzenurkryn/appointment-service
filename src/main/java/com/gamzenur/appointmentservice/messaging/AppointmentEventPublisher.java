package com.gamzenur.appointmentservice.messaging;

import com.gamzenur.appointmentservice.dto.AppointmentResponse;

public interface AppointmentEventPublisher {

    void publishCreated(AppointmentResponse appointment);

    void publishUpdated(AppointmentResponse appointment);

    default void publishUpdated(AppointmentResponse appointment, String correlationId) {
        publishUpdated(appointment);
    }

    void publishCancelled(AppointmentResponse appointment);

    default void publishCancelled(AppointmentResponse appointment, String correlationId) {
        publishCancelled(appointment);
    }
}
