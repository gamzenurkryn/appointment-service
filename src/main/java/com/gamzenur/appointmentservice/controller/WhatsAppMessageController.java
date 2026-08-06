package com.gamzenur.appointmentservice.controller;

import com.gamzenur.appointmentservice.dto.SendWhatsAppMessageRequest;
import com.gamzenur.appointmentservice.dto.SendWhatsAppMessageResponse;
import com.gamzenur.appointmentservice.whatsapp.WhatsAppMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/whatsapp/messages")
@Tag(name = "WhatsApp Mesajları", description = "Meta WhatsApp Cloud API üzerinden giden mesaj işlemleri")
public class WhatsAppMessageController {

    private final WhatsAppMessageService messageService;

    public WhatsAppMessageController(WhatsAppMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @Operation(summary = "WhatsApp mesajı gönder", description = "Yapılandırılmış Meta test veya üretim numarasına metin mesajı gönderir.")
    public ResponseEntity<SendWhatsAppMessageResponse> sendTextMessage(
            @Valid @RequestBody SendWhatsAppMessageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(messageService.sendTextMessage(request));
    }
}
