package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.StoreResponse;
import com.gamzenur.appointmentservice.entity.Store;
import com.gamzenur.appointmentservice.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<StoreResponse> getStores() {
        return storeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private StoreResponse toResponse(Store store) {
        StoreResponse response = new StoreResponse();
        response.setId(store.getId());
        response.setName(store.getName());
        response.setLocation(store.getLocation());
        response.setPhone(store.getPhone());
        response.setGoogleCalendarId(store.getGoogleCalendarId());
        response.setTimezone(store.getTimezone());
        return response;
    }
}
