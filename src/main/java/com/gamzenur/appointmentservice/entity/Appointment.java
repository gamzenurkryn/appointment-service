package com.gamzenur.appointmentservice.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

//veritabanı tablosu olduğunu belrtir
@Entity
@Table(name = "appointments")
public class Appointment {

    //benzersiz kimlik UUID
    @Id
    @GeneratedValue
    private UUID id;

    private String customerName;

    private String customerPhone;

    private UUID storeId;

    private String storeName;

    private String serviceType;

    //OffsetDateTime zaman dilgisi
    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    //sadece belirlenen durum
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    private String calendarEventId;

    //uzun notlar için text
    @Column(columnDefinition = "TEXT")
    private  String notes;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;


    //sınıftaki verilere erişme
    public Appointment() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

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

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }




    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public AppointmentStatus getStatus() {return status;}

    public void setStatus(AppointmentStatus status) {this.status = status;}

    public String getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(String calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    }



