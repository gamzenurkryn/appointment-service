package com.gamzenur.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SendWhatsAppMessageRequest {

    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "telefon +905xxxxxxxxx biçiminde olmalıdır")
    private String to;

    @NotBlank
    @Size(max = 4096, message = "mesaj en fazla 4096 karakter olabilir")
    private String text;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
