package com.gamzenur.callservice.integration.appointment;

import java.util.UUID;

public interface AppointmentClient {

    AppointmentSummary getAppointment(UUID appointmentId);
}
