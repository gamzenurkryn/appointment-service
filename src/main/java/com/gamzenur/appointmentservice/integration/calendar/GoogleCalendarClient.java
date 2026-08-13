package com.gamzenur.appointmentservice.integration.calendar;

import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.Store;

public interface GoogleCalendarClient {

    String createEvent(Store store, Appointment appointment);

    void updateEvent(Store store, Appointment appointment);

    void deleteEvent(Store store, String calendarEventId);
}
