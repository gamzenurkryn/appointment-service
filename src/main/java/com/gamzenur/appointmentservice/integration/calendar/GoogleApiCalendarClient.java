package com.gamzenur.appointmentservice.integration.calendar;

import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.Store;
import com.gamzenur.appointmentservice.exception.CalendarIntegrationException;
import com.gamzenur.appointmentservice.service.ServiceCatalog;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(
        name = "integration.google-calendar.enabled",
        havingValue = "true"
)
public class GoogleApiCalendarClient implements GoogleCalendarClient {

    private final Calendar calendarService;

    public GoogleApiCalendarClient(Calendar calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public String createEvent(Store store, Appointment appointment) {
        String calendarId = requireCalendarId(store);
        try {
            Event createdEvent = calendarService.events()
                    .insert(calendarId, toGoogleEvent(store, appointment))
                    .execute();
            return createdEvent.getId();
        } catch (IOException exception) {
            throw new CalendarIntegrationException("Google Calendar etkinliği oluşturulamadı.", exception);
        }
    }

    @Override
    public void updateEvent(Store store, Appointment appointment) {
        String calendarId = requireCalendarId(store);
        try {
            calendarService.events()
                    .update(calendarId, appointment.getCalendarEventId(), toGoogleEvent(store, appointment))
                    .execute();
        } catch (IOException exception) {
            throw new CalendarIntegrationException("Google Calendar etkinliği güncellenemedi.", exception);
        }
    }

    @Override
    public void deleteEvent(Store store, String calendarEventId) {
        String calendarId = requireCalendarId(store);
        try {
            calendarService.events().delete(calendarId, calendarEventId).execute();
        } catch (IOException exception) {
            throw new CalendarIntegrationException("Google Calendar etkinliği silinemedi.", exception);
        }
    }

    private Event toGoogleEvent(Store store, Appointment appointment) {
        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(appointment.getStartTime().toInstant().toEpochMilli()))
                .setTimeZone(store.getTimezone());
        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(appointment.getEndTime().toInstant().toEpochMilli()))
                .setTimeZone(store.getTimezone());

        return new Event()
                .setSummary(ServiceCatalog.displayName(appointment.getServiceType())
                        + " - " + appointment.getCustomerName())
                .setDescription(appointment.getNotes())
                .setLocation(store.getLocation())
                .setStart(start)
                .setEnd(end);
    }

    private String requireCalendarId(Store store) {
        if (store.getGoogleCalendarId() == null || store.getGoogleCalendarId().isBlank()) {
            throw new CalendarIntegrationException(
                    "Mağaza için googleCalendarId tanımlanmamış: " + store.getId()
            );
        }
        return store.getGoogleCalendarId();
    }
}
