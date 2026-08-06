package com.gamzenur.crmservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "offers", schema = "crm_service")
public class Offer {

    @Id
    private UUID id;
    @Column(name = "appointment_id", nullable = false, unique = true)
    private UUID appointmentId;
    @Column(name = "store_id", nullable = false)
    private UUID storeId;
    @Column(name = "source_service_type", nullable = false)
    private String sourceServiceType;
    @Column(name = "offered_service_type", nullable = false)
    private String offeredServiceType;
    @Column(nullable = false)
    private String title;
    @Column(name = "discount_percent", nullable = false)
    private int discountPercent;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;
    @Column(name = "correlation_id", nullable = false)
    private String correlationId;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAppointmentId() { return appointmentId; }
    public void setAppointmentId(UUID appointmentId) { this.appointmentId = appointmentId; }
    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }
    public String getSourceServiceType() { return sourceServiceType; }
    public void setSourceServiceType(String sourceServiceType) { this.sourceServiceType = sourceServiceType; }
    public String getOfferedServiceType() { return offeredServiceType; }
    public void setOfferedServiceType(String offeredServiceType) { this.offeredServiceType = offeredServiceType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
