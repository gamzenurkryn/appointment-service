package com.gamzenur.callservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateCallRequest {

    @NotNull
    private UUID appointmentId;

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }
}
