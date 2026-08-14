package com.gamzenur.appointmentservice.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class TimeSlotResponse {

    private OffsetDateTime start;
    private OffsetDateTime end;
    private List<AvailableEmployeeResponse> availableEmployees;

    public TimeSlotResponse() {
    }

    public TimeSlotResponse(OffsetDateTime start, OffsetDateTime end) {
        this.start = start;
        this.end = end;
    }

    public TimeSlotResponse(
            OffsetDateTime start,
            OffsetDateTime end,
            List<AvailableEmployeeResponse> availableEmployees
    ) {
        this.start = start;
        this.end = end;
        this.availableEmployees = availableEmployees;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public void setStart(OffsetDateTime start) {
        this.start = start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public void setEnd(OffsetDateTime end) {
        this.end = end;
    }

    public List<AvailableEmployeeResponse> getAvailableEmployees() {
        return availableEmployees;
    }

    public void setAvailableEmployees(List<AvailableEmployeeResponse> availableEmployees) {
        this.availableEmployees = availableEmployees;
    }
}
