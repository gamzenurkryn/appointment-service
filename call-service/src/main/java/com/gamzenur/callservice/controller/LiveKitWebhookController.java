package com.gamzenur.callservice.controller;

import com.gamzenur.callservice.service.CallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitWebhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/v1/livekit")
@Tag(name = "LiveKit Webhook", description = "LiveKit oda ve katılımcı olaylarını işler")
public class LiveKitWebhookController {

    private final CallService callService;
    private final WebhookReceiver webhookReceiver;

    public LiveKitWebhookController(
            CallService callService,
            @Value("${integration.livekit.api-key}") String apiKey,
            @Value("${integration.livekit.api-secret}") String apiSecret
    ) {
        this.callService = callService;
        this.webhookReceiver = new WebhookReceiver(apiKey, apiSecret);
    }

    @PostMapping(value = "/webhook", consumes = {
            "application/webhook+json",
            MediaType.APPLICATION_JSON_VALUE
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "LiveKit webhook olayını al", description = "İmzayı doğrular ve ilgili çağrı kaydının durumunu günceller.")
    public void receiveWebhook(
            @RequestBody String rawBody,
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        if (authorization == null || authorization.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LiveKit imzası eksik.");
        }

        final LivekitWebhook.WebhookEvent event;
        try {
            event = webhookReceiver.receive(rawBody, authorization);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LiveKit imzası geçersiz.", exception);
        }

        if (!event.hasRoom() || event.getRoom().getName().isBlank()) {
            return;
        }

        OffsetDateTime eventTime = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(event.getCreatedAt()),
                ZoneOffset.UTC
        );
        callService.handleLiveKitEvent(
                event.getEvent(),
                event.getRoom().getName(),
                event.getRoom().getNumParticipants(),
                eventTime
        );
    }
}
