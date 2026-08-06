package com.gamzenur.appointmentservice.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.appointmentservice.config.WhatsAppProperties;
import com.gamzenur.appointmentservice.exception.WhatsAppIntegrationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "integration.whatsapp", name = "cloud-api-enabled", havingValue = "true")
public class MetaWhatsAppCloudApiClient implements WhatsAppCloudApiClient {

    private final WhatsAppProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MetaWhatsAppCloudApiClient(
            WhatsAppProperties properties,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl("https://graph.facebook.com").build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String sendTextMessage(String recipientPhone, String text) {
        validateConfiguration();

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", recipientPhone,
                "type", "text",
                "text", Map.of("preview_url", false, "body", text)
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/{version}/{phoneNumberId}/messages",
                            properties.getGraphApiVersion(), properties.getPhoneNumberId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(properties.getAccessToken()))
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode messageId = response == null ? null : response.at("/messages/0/id");
            if (messageId == null || messageId.isMissingNode() || messageId.asText().isBlank()) {
                throw new WhatsAppIntegrationException("Meta yanıtında mesaj kimliği bulunamadı.");
            }
            return messageId.asText();
        } catch (WhatsAppIntegrationException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw new WhatsAppIntegrationException(formatMetaError(exception), exception);
        } catch (RestClientException exception) {
            throw new WhatsAppIntegrationException("Mesaj Meta WhatsApp API'ye gönderilemedi.", exception);
        }
    }

    String formatMetaError(RestClientResponseException exception) {
        String metaCode = "bilinmiyor";
        String metaMessage = "Meta isteği reddetti.";

        try {
            JsonNode error = objectMapper.readTree(exception.getResponseBodyAsString()).path("error");
            if (!error.path("code").isMissingNode()) {
                metaCode = error.path("code").asText();
            }
            if (!error.path("message").asText().isBlank()) {
                metaMessage = error.path("message").asText();
            }
        } catch (Exception ignored) {
            // Meta JSON dışında cevap döndürürse güvenli varsayılan açıklama kullanılır.
        }

        if (StringUtils.hasText(properties.getAccessToken())) {
            metaMessage = metaMessage.replace(properties.getAccessToken(), "[gizlendi]");
        }
        if (metaMessage.length() > 300) {
            metaMessage = metaMessage.substring(0, 300);
        }

        return "Meta WhatsApp API hatası (HTTP " + exception.getStatusCode().value()
                + ", kod " + metaCode + "): " + metaMessage;
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getPhoneNumberId())
                || !StringUtils.hasText(properties.getAccessToken())
                || !StringUtils.hasText(properties.getGraphApiVersion())) {
            throw new WhatsAppIntegrationException("WhatsApp Cloud API ayarları eksik.");
        }
    }
}
