package com.gamzenur.appointmentservice.messaging;

import com.gamzenur.appointmentservice.dto.AppointmentResponse;

public interface AppointmentEventPublisher {

    void publishCreated(AppointmentResponse appointment);

    void publishUpdated(AppointmentResponse appointment);

    void publishCancelled(AppointmentResponse appointment);
}
