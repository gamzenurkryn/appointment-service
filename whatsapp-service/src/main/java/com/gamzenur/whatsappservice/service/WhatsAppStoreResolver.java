package com.gamzenur.whatsappservice.service;

import com.gamzenur.whatsappservice.exception.WhatsAppExceptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class WhatsAppStoreResolver {

    private final Map<String, UUID> storeIdsByPhoneNumberId;

    public WhatsAppStoreResolver(
            @Value("${integration.whatsapp.store-mappings:}") String mappings) {
        this.storeIdsByPhoneNumberId = parseMappings(mappings);
    }

    public UUID resolve(String phoneNumberId) {
        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            throw new WhatsAppExceptions.BadRequest(
                    "WhatsApp webhook metadata.phone_number_id alanı bulunamadı."
            );
        }

        UUID storeId = storeIdsByPhoneNumberId.get(phoneNumberId);
        if (storeId == null) {
            throw new WhatsAppExceptions.BadRequest(
                    "WhatsApp phone_number_id için mağaza eşlemesi bulunamadı: " + phoneNumberId
            );
        }
        return storeId;
    }

    private Map<String, UUID> parseMappings(String mappings) {
        if (mappings == null || mappings.isBlank()) {
            return Map.of();
        }

        try {
            return Arrays.stream(mappings.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .map(value -> value.split("=", 2))
                    .collect(Collectors.toUnmodifiableMap(
                            parts -> parts[0].trim(),
                            parts -> UUID.fromString(parts[1].trim())
                    ));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "WHATSAPP_STORE_MAPPINGS değeri phoneNumberId=storeUUID biçiminde olmalıdır.",
                    exception
            );
        }
    }
}
