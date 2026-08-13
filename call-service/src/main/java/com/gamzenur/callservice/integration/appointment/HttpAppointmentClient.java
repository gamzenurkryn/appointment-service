package com.gamzenur.callservice.integration.appointment;

import com.gamzenur.callservice.exception.CallServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class HttpAppointmentClient implements AppointmentClient {

    private final RestClient restClient;

    public HttpAppointmentClient(
            RestClient.Builder builder,
            @Value("${integration.appointment-service.base-url}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public AppointmentSummary getAppointment(UUID appointmentId) {
        try {
            AppointmentSummary appointment = restClient.get()
                    .uri("/api/v1/appointments/{id}", appointmentId)
                    .retrieve()
                    .body(AppointmentSummary.class);
            if (appointment == null) {
                throw new CallServiceException("Randevu bilgisi alınamadı.");
            }
            return appointment;
        } catch (RestClientException exception) {
            throw new CallServiceException("Appointment service ile iletişim kurulamadı.", exception);
        }
    }
}
