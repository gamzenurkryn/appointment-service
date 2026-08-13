package com.gamzenur.callservice.dto;

import com.gamzenur.callservice.domain.CallResult;
import com.gamzenur.callservice.domain.CallStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class UpdateCallRequest {

    private CallStatus status;
    private CallResult result;

    @Min(0)
    private Integer participantCount;

    @Size(max = 1000)
    private String transcriptUrl;

    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;

    public CallStatus getStatus() { return status; }
    public void setStatus(CallStatus status) { this.status = status; }
    public CallResult getResult() { return result; }
    public void setResult(CallResult result) { this.result = result; }
    public Integer getParticipantCount() { return participantCount; }
    public void setParticipantCount(Integer participantCount) { this.participantCount = participantCount; }
    public String getTranscriptUrl() { return transcriptUrl; }
    public void setTranscriptUrl(String transcriptUrl) { this.transcriptUrl = transcriptUrl; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(OffsetDateTime endedAt) { this.endedAt = endedAt; }
}
