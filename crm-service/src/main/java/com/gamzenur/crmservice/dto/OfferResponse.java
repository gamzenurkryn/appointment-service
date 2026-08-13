package com.gamzenur.crmservice.dto;

import com.gamzenur.crmservice.domain.OfferStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OfferResponse(
        UUID id,
        UUID appointmentId,
        UUID storeId,
        String sourceServiceType,
        String offeredServiceType,
        String title,
        int discountPercent,
        OfferStatus status,
        String correlationId,
        OffsetDateTime createdAt
) {
}
