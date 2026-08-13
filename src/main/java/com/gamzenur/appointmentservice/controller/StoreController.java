package com.gamzenur.appointmentservice.controller;

import com.gamzenur.appointmentservice.dto.StoreResponse;
import com.gamzenur.appointmentservice.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@Tag(name = "Mağazalar", description = "Randevu alınabilen şubeleri listeler")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    @Operation(summary = "Mağazaları listele")
    public List<StoreResponse> getStores() {
        return storeService.getStores();
    }
}
