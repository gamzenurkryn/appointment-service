package com.gamzenur.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public class CreateEmployeeRequest {

    @NotNull
    private UUID storeId;

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotEmpty
    private Set<String> serviceTypes;

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

    public Set<String> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(Set<String> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }
}