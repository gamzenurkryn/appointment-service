package com.gamzenur.appointmentservice.controller;

import com.gamzenur.appointmentservice.dto.StoreContextResponse;
import com.gamzenur.appointmentservice.service.StoreContextService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreContextController {

    private final StoreContextService storeContextService;

    public StoreContextController(StoreContextService storeContextService) {
        this.storeContextService = storeContextService;
    }

    @GetMapping("/{storeId}/context")
    public StoreContextResponse getStoreContext(@PathVariable UUID storeId) {
        return storeContextService.getContext(storeId);
    }
}
