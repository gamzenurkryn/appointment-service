package com.gamzenur.callservice.integration.appointment;

import java.util.UUID;

public class AppointmentSummary {

    private UUID id;
    private String customerPhone;
    private UUID storeId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }
}
