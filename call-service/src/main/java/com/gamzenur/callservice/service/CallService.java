package com.gamzenur.callservice.service;

import com.gamzenur.callservice.domain.Call;
import com.gamzenur.callservice.domain.CallStatus;
import com.gamzenur.callservice.dto.CallResponse;
import com.gamzenur.callservice.dto.CreateCallRequest;
import com.gamzenur.callservice.dto.UpdateCallRequest;
import com.gamzenur.callservice.exception.CallNotFoundException;
import com.gamzenur.callservice.integration.appointment.AppointmentClient;
import com.gamzenur.callservice.integration.appointment.AppointmentSummary;
import com.gamzenur.callservice.integration.livekit.AgentDispatcher;
import com.gamzenur.callservice.integration.livekit.LiveKitClient;
import com.gamzenur.callservice.messaging.CallEventPublisher;
import com.gamzenur.callservice.repository.CallRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CallService {

    private final CallRepository callRepository;
    private final AppointmentClient appointmentClient;
    private final LiveKitClient liveKitClient;
    private final AgentDispatcher agentDispatcher;
    private final CallEventPublisher callEventPublisher;

    public CallService(
            CallRepository callRepository,
            AppointmentClient appointmentClient,
            LiveKitClient liveKitClient,
            AgentDispatcher agentDispatcher,
            CallEventPublisher callEventPublisher
    ) {
        this.callRepository = callRepository;
        this.appointmentClient = appointmentClient;
        this.liveKitClient = liveKitClient;
        this.agentDispatcher = agentDispatcher;
        this.callEventPublisher = callEventPublisher;
    }

    @Transactional
    public CallResponse createCall(CreateCallRequest request) {
        return createCall(request, null);
    }

    @Transactional
    public CallResponse createCall(CreateCallRequest request, String correlationId) {
        Call existingCall = callRepository.findFirstByAppointmentId(request.getAppointmentId()).orElse(null);
        if (existingCall != null) {
            return toResponse(existingCall);
        }

        AppointmentSummary appointment = appointmentClient.getAppointment(request.getAppointmentId());
        UUID callId = UUID.randomUUID();
        String roomName = "call-" + callId.toString().substring(0, 8);

        liveKitClient.createRoom(roomName);
        agentDispatcher.dispatch(roomName, callId, appointment.getId());

        Call call = new Call();
        call.setId(callId);
        call.setAppointmentId(appointment.getId());
        call.setRoomName(roomName);
        call.setCustomerPhone(appointment.getCustomerPhone());
        call.setStoreId(appointment.getStoreId());
        call.setStatus(CallStatus.QUEUED);
        call.setParticipantCount(0);
        CallResponse response = toResponse(callRepository.save(call));
        callEventPublisher.publish("call.started", response, correlationId);
        return response;
    }

    @Transactional(readOnly = true)
    public List<CallResponse> getCalls(CallStatus status, UUID storeId, String query) {
        String normalizedQuery = query == null || query.isBlank() ? "" : query.trim();
        List<Call> calls = callRepository.search(status, storeId, normalizedQuery);
        return calls.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CallResponse getCall(UUID id) {
        return toResponse(findCall(id));
    }

    @Transactional
    public CallResponse updateCall(UUID id, UpdateCallRequest request) {
        return updateCall(id, request, null);
    }

    @Transactional
    public CallResponse updateCall(UUID id, UpdateCallRequest request, String correlationId) {
        Call call = findCall(id);
        if (request.getStatus() != null) call.setStatus(request.getStatus());
        if (request.getResult() != null) call.setResult(request.getResult());
        if (request.getParticipantCount() != null) call.setParticipantCount(request.getParticipantCount());
        if (request.getTranscriptUrl() != null) call.setTranscriptUrl(request.getTranscriptUrl());
        if (request.getStartedAt() != null) call.setStartedAt(request.getStartedAt());
        if (request.getEndedAt() != null) call.setEndedAt(request.getEndedAt());
        CallResponse response = toResponse(callRepository.save(call));
        callEventPublisher.publish(eventTypeFor(call.getStatus()), response, correlationId);
        return response;
    }

    @Transactional
    public void handleLiveKitEvent(
            String eventName,
            String roomName,
            int participantCount,
            OffsetDateTime eventTime
    ) {
        Call call = callRepository.findByRoomName(roomName).orElse(null);
        if (call == null) {
            return;
        }

        switch (eventName) {
            case "room_started" -> {
                call.setStatus(CallStatus.ACTIVE);
                if (call.getStartedAt() == null) {
                    call.setStartedAt(eventTime);
                }
            }
            case "participant_joined", "participant_left" -> {
                call.setParticipantCount(Math.max(participantCount, 0));
                if ("participant_joined".equals(eventName)) {
                    call.setStatus(CallStatus.ACTIVE);
                    if (call.getStartedAt() == null) {
                        call.setStartedAt(eventTime);
                    }
                }
            }
            case "room_finished" -> {
                call.setStatus(CallStatus.COMPLETED);
                call.setParticipantCount(0);
                call.setEndedAt(eventTime);
            }
            default -> {
                return;
            }
        }

        CallResponse response = toResponse(callRepository.save(call));
        callEventPublisher.publish(eventTypeFor(call.getStatus()), response);
    }

    private String eventTypeFor(CallStatus status) {
        return switch (status) {
            case COMPLETED, FAILED, NO_ANSWER -> "call.ended";
            default -> "call.updated";
        };
    }

    private Call findCall(UUID id) {
        return callRepository.findById(id).orElseThrow(() -> new CallNotFoundException(id));
    }

    private CallResponse toResponse(Call call) {
        CallResponse response = new CallResponse();
        response.setId(call.getId());
        response.setAppointmentId(call.getAppointmentId());
        response.setRoomName(call.getRoomName());
        response.setCustomerPhone(call.getCustomerPhone());
        response.setStoreId(call.getStoreId());
        response.setStatus(call.getStatus());
        response.setResult(call.getResult());
        response.setParticipantCount(call.getParticipantCount());
        response.setTranscriptUrl(call.getTranscriptUrl());
        response.setStartedAt(call.getStartedAt());
        response.setEndedAt(call.getEndedAt());
        return response;
    }
}
