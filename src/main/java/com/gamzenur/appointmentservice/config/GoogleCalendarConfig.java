package com.gamzenur.appointmentservice.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
@ConditionalOnProperty(
        name = "integration.google-calendar.enabled",
        havingValue = "true"
)
public class GoogleCalendarConfig {

    @Bean
    public Calendar googleCalendarService(
            @Value("${integration.google-calendar.credentials-location}") Resource credentialsResource,
            @Value("${integration.google-calendar.application-name}") String applicationName
    ) throws IOException, GeneralSecurityException {
        GoogleCredentials credentials;
        try (var inputStream = credentialsResource.getInputStream()) {
            credentials = ServiceAccountCredentials.fromStream(inputStream)
                    .createScoped(List.of(CalendarScopes.CALENDAR));
        }

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName(applicationName)
                .build();
    }
}
