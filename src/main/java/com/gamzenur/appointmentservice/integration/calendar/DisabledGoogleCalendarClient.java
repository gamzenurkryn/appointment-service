package com.gamzenur.appointmentservice.integration.calendar;

import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.Store;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "integration.google-calendar.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class DisabledGoogleCalendarClient implements GoogleCalendarClient {

    @Override
    public String createEvent(Store store, Appointment appointment) {
        return null;
    }

    @Override
    public void updateEvent(Store store, Appointment appointment) {
        // Google Calendar entegrasyonu kapalıyken işlem yapılmaz.
    }

    @Override
    public void deleteEvent(Store store, String calendarEventId) {
        // Google Calendar entegrasyonu kapalıyken işlem yapılmaz.
    }
}
