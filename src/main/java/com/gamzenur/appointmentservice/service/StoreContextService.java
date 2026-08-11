package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.StoreContextResponse;
import com.gamzenur.appointmentservice.dto.StoreServiceResponse;
import com.gamzenur.appointmentservice.entity.Store;
import com.gamzenur.appointmentservice.exception.ResourceNotFoundException;
import com.gamzenur.appointmentservice.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StoreContextService {

    private static final LocalTime OPENING_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);
    private static final int SLOT_DURATION_MINUTES = 30;

    private final StoreRepository storeRepository;

    public StoreContextService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public StoreContextResponse getContext(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mağaza bulunamadı: " + storeId));

        List<StoreServiceResponse> services = ServiceCatalog.supportedTypes().stream()
                .map(code -> new StoreServiceResponse(code, ServiceCatalog.displayName(code)))
                .toList();

        return new StoreContextResponse(
                store.getId(),
                store.getName(),
                store.getLocation(),
                store.getTimezone(),
                OPENING_TIME,
                CLOSING_TIME,
                SLOT_DURATION_MINUTES,
                services
        );
    }
}
