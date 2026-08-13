package com.gamzenur.whatsappservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.whatsappservice.config.WhatsAppProperties;
import com.gamzenur.whatsappservice.exception.WhatsAppExceptions;
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
    private final RestClient client;
    private final ObjectMapper objectMapper;
    public MetaWhatsAppCloudApiClient(WhatsAppProperties properties, RestClient.Builder builder,
                                      ObjectMapper objectMapper) {
        this.properties = properties;
        this.client = builder.baseUrl("https://graph.facebook.com").build();
        this.objectMapper = objectMapper;
    }
    @Override public String sendTextMessage(String recipientPhone, String text) {
        if (!StringUtils.hasText(properties.getPhoneNumberId()) || !StringUtils.hasText(properties.getAccessToken()))
            throw new WhatsAppExceptions.NotConfigured("WhatsApp Cloud API ayarları eksik.");
        try {
            JsonNode response = client.post().uri("/{version}/{phoneNumberId}/messages",
                    properties.getGraphApiVersion(), properties.getPhoneNumberId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> h.setBearerAuth(properties.getAccessToken()))
                    .body(Map.of("messaging_product", "whatsapp", "to", recipientPhone, "type", "text",
                            "text", Map.of("preview_url", false, "body", text)))
                    .retrieve().body(JsonNode.class);
            JsonNode id = response == null ? null : response.at("/messages/0/id");
            if (id == null || id.isMissingNode() || id.asText().isBlank())
                throw new WhatsAppExceptions.Integration("Meta yanıtında mesaj kimliği bulunamadı.");
            return id.asText();
        } catch (WhatsAppExceptions.Integration ex) { throw ex; }
        catch (RestClientResponseException ex) {
            throw new WhatsAppExceptions.Integration(formatMetaError(ex), ex);
        }
        catch (RestClientException ex) {
            throw new WhatsAppExceptions.Integration("Mesaj Meta WhatsApp API'ye gönderilemedi.", ex);
        }
    }

    private String formatMetaError(RestClientResponseException exception) {
        String code = "bilinmiyor";
        String message = "Meta isteği reddetti.";
        try {
            JsonNode error = objectMapper.readTree(exception.getResponseBodyAsString()).path("error");
            if (!error.path("code").isMissingNode()) code = error.path("code").asText();
            if (!error.path("message").asText().isBlank()) message = error.path("message").asText();
        } catch (Exception ignored) { }
        if (StringUtils.hasText(properties.getAccessToken()))
            message = message.replace(properties.getAccessToken(), "[gizlendi]");
        if (message.length() > 300) message = message.substring(0, 300);
        return "Meta WhatsApp API hatası (HTTP " + exception.getStatusCode().value()
                + ", kod " + code + "): " + message;
    }
}
