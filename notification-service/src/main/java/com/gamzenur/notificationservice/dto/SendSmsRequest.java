package com.gamzenur.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SendSmsRequest(
        @NotBlank(message = "Telefon numarası zorunludur.")
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Telefon numarası E.164 biçiminde olmalıdır.")
        String phone,

        @NotBlank(message = "Mesaj zorunludur.")
        @Size(max = 500, message = "Mesaj en fazla 500 karakter olabilir.")
        String message
) {
}
