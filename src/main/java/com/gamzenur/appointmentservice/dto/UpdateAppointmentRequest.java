package com.gamzenur.appointmentservice.dto;

import com.gamzenur.appointmentservice.entity.AppointmentStatus;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public class UpdateAppointmentRequest {

    private OffsetDateTime startTime;
    private AppointmentStatus status;
    @Size(max = 2000)
    private String notes;

    public UpdateAppointmentRequest() {
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime){
        this.startTime = startTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
