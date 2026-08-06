package com.gamzenur.appointmentservice.controller;

import com.gamzenur.appointmentservice.dto.AvailabilityResponse;
import com.gamzenur.appointmentservice.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/availability")
@Tag(name = "Uygunluk", description = "Mağazaların boş randevu saatlerini hesaplar")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    @Operation(summary = "Uygun saatleri getir", description = "Mağaza, tarih ve hizmet türüne göre boş 30 dakikalık zaman dilimlerini döndürür.")
    public AvailabilityResponse getAvailability(
            @RequestParam UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String serviceType) {

        return availabilityService.getAvailability(storeId, date, serviceType);
    }
}
