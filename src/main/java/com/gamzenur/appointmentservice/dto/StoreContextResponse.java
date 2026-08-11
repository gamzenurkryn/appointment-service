package com.gamzenur.appointmentservice.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record StoreContextResponse(
        UUID storeId,
        String storeName,
        String location,
        String timezone,
        LocalTime openingTime,
        LocalTime closingTime,
        int slotDurationMinutes,
        List<StoreServiceResponse> services
) {
}
