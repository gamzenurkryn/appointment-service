package com.gamzenur.crmservice.controller;

import com.gamzenur.crmservice.dto.OfferResponse;
import com.gamzenur.crmservice.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/offers")
@Tag(name = "CRM Teklifleri", description = "Randevu olaylarından üretilen ek hizmet teklifleri")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    @Operation(summary = "Upsell tekliflerini listele", description = "İstenirse appointmentId ile tek randevunun tekliflerini filtreler.")
    public List<OfferResponse> getOffers(@RequestParam(required = false) UUID appointmentId) {
        return offerService.getOffers(appointmentId);
    }
}
