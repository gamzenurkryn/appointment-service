package com.gamzenur.appointmentservice.dto;

import java.time.OffsetDateTime;

public class TimeSlotResponse {

    private OffsetDateTime start;
    private OffsetDateTime end;

    public TimeSlotResponse() {
    }

    public TimeSlotResponse(OffsetDateTime start, OffsetDateTime end) {
        this.start = start;
        this.end = end;
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
}