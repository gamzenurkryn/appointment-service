package com.gamzenur.whatsappservice.controller;

import com.gamzenur.whatsappservice.dto.SendWhatsAppMessageRequest;
import com.gamzenur.whatsappservice.dto.SendWhatsAppMessageResponse;
import com.gamzenur.whatsappservice.service.WhatsAppMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/whatsapp/messages")
public class WhatsAppMessageController {

    private final WhatsAppMessageService service;

    public WhatsAppMessageController(WhatsAppMessageService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SendWhatsAppMessageResponse> send(
            @Valid @RequestBody SendWhatsAppMessageRequest request) {

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(service.send(request));
    }
}