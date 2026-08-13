package com.gamzenur.callservice.dto;

import com.gamzenur.callservice.domain.CallResult;
import com.gamzenur.callservice.domain.CallStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CallResponse {

    private UUID id;
    private UUID appointmentId;
    private String roomName;
    private String customerPhone;
    private UUID storeId;
    private CallStatus status;
    private CallResult result;
    private int participantCount;
    private String transcriptUrl;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAppointmentId() { return appointmentId; }
    public void setAppointmentId(UUID appointmentId) { this.appointmentId = appointmentId; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }
    public CallStatus getStatus() { return status; }
    public void setStatus(CallStatus status) { this.status = status; }
    public CallResult getResult() { return result; }
    public void setResult(CallResult result) { this.result = result; }
    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }
    public String getTranscriptUrl() { return transcriptUrl; }
    public void setTranscriptUrl(String transcriptUrl) { this.transcriptUrl = transcriptUrl; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(OffsetDateTime endedAt) { this.endedAt = endedAt; }
}
