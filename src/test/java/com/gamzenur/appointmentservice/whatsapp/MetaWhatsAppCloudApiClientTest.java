package com.gamzenur.appointmentservice.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.appointmentservice.config.WhatsAppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MetaWhatsAppCloudApiClientTest {

    @Test
    void shouldExposeSafeMetaErrorDetailsWithoutAccessToken() {
        WhatsAppProperties properties = new WhatsAppProperties();
        properties.setAccessToken("secret-token");
        MetaWhatsAppCloudApiClient client = new MetaWhatsAppCloudApiClient(
                properties,
                RestClient.builder(),
                new ObjectMapper()
        );
        String body = "{\"error\":{\"message\":\"Token secret-token has expired\",\"code\":190}}";
        RestClientResponseException exception = new RestClientResponseException(
                "Unauthorized",
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                HttpHeaders.EMPTY,
                body.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        String message = client.formatMetaError(exception);

        assertThat(message).contains("HTTP 401", "kod 190", "[gizlendi] has expired");
        assertThat(message).doesNotContain("secret-token");
    }
}
