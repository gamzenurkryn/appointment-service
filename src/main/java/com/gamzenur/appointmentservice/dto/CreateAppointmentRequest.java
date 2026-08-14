package com.gamzenur.appointmentservice.dto;

import com.gamzenur.appointmentservice.entity.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CreateAppointmentRequest {

    @NotBlank
    @Size(max = 150)
    private String customerName;

    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "E.164 formatında olmalıdır (örnek: +905321234567)")
    private String customerPhone;

    @NotNull
    private UUID storeId;
    private UUID employeeId;

    @NotBlank
    @Size(max = 100)
    private String serviceType;

    @NotNull
    private OffsetDateTime startTime;

    @NotNull
    private Channel channel;

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public UUID getEmployeeId() { return employeeId; }

    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


}
