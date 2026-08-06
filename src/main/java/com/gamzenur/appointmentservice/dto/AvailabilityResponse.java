package com.gamzenur.appointmentservice.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AvailabilityResponse {

    private UUID storeId;
    private LocalDate date;
    private List<TimeSlotResponse> slots;

    public AvailabilityResponse() {
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<TimeSlotResponse> getSlots() {
        return slots;
    }

    public void setSlots(List<TimeSlotResponse> slots) {
        this.slots = slots;
    }
}