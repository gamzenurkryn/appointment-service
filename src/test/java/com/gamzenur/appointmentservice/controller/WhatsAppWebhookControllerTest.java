package com.gamzenur.appointmentservice.controller;

import com.gamzenur.appointmentservice.config.WhatsAppProperties;
import com.gamzenur.appointmentservice.whatsapp.WhatsAppWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class WhatsAppWebhookControllerTest {

    private WhatsAppWebhookController controller;

    @BeforeEach
    void setUp() {
        WhatsAppProperties properties = new WhatsAppProperties();
        properties.setVerifyToken("test-verify-token");
        properties.setAppSecret("test-app-secret");
        controller = new WhatsAppWebhookController(
                properties,
                mock(WhatsAppWebhookService.class)
        );
    }

    @Test
    void correctVerifyTokenReturnsChallenge() {
        ResponseEntity<String> response = controller.verifyWebhook(
                "subscribe",
                "test-verify-token",
                "challenge-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("challenge-123", response.getBody());
    }

    @Test
    void incorrectVerifyTokenIsForbidden() {
        ResponseEntity<String> response = controller.verifyWebhook(
                "subscribe",
                "wrong-token",
                "challenge-123"
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
