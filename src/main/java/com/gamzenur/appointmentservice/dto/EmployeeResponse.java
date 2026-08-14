package com.gamzenur.appointmentservice.dto;

import java.util.Set;
import java.util.UUID;

public class EmployeeResponse {

    private UUID id;
    private UUID storeId;
    private String name;
    private boolean active;
    private Set<String> serviceTypes;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<String> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(Set<String> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }
}