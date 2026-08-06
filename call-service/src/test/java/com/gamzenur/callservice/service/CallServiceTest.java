package com.gamzenur.callservice.service;

import com.gamzenur.callservice.domain.Call;
import com.gamzenur.callservice.domain.CallStatus;
import com.gamzenur.callservice.dto.CallResponse;
import com.gamzenur.callservice.dto.CreateCallRequest;
import com.gamzenur.callservice.integration.appointment.AppointmentClient;
import com.gamzenur.callservice.integration.appointment.AppointmentSummary;
import com.gamzenur.callservice.integration.livekit.AgentDispatcher;
import com.gamzenur.callservice.integration.livekit.LiveKitClient;
import com.gamzenur.callservice.messaging.CallEventPublisher;
import com.gamzenur.callservice.repository.CallRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CallServiceTest {

    @Test
    void shouldCreateQueuedCallFromAppointment() {
        UUID appointmentId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        AppointmentSummary appointment = new AppointmentSummary();
        appointment.setId(appointmentId);
        appointment.setStoreId(storeId);
        appointment.setCustomerPhone("+905321234567");

        AppointmentClient appointmentClient = mock(AppointmentClient.class);
        LiveKitClient liveKitClient = mock(LiveKitClient.class);
        AgentDispatcher agentDispatcher = mock(AgentDispatcher.class);
        CallEventPublisher callEventPublisher = mock(CallEventPublisher.class);
        CallRepository repository = mock(CallRepository.class);
        when(appointmentClient.getAppointment(appointmentId)).thenReturn(appointment);
        when(repository.save(any(Call.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CallService service = new CallService(
                repository,
                appointmentClient,
                liveKitClient,
                agentDispatcher,
                callEventPublisher
        );
        CreateCallRequest request = new CreateCallRequest();
        request.setAppointmentId(appointmentId);

        CallResponse response = service.createCall(request);

        assertThat(response.getAppointmentId()).isEqualTo(appointmentId);
        assertThat(response.getStoreId()).isEqualTo(storeId);
        assertThat(response.getCustomerPhone()).isEqualTo("+905321234567");
        assertThat(response.getRoomName()).startsWith("call-");
        assertThat(response.getStatus()).isEqualTo(CallStatus.QUEUED);
        assertThat(response.getParticipantCount()).isZero();
        verify(liveKitClient).createRoom(response.getRoomName());
        verify(agentDispatcher).dispatch(response.getRoomName(), response.getId(), appointmentId);
        verify(callEventPublisher).publish("call.started", response);
    }

    @Test
    void shouldCompleteCallWhenLiveKitRoomFinishes() {
        CallRepository repository = mock(CallRepository.class);
        Call call = new Call();
        call.setId(UUID.randomUUID());
        call.setRoomName("call-test1234");
        call.setStatus(CallStatus.ACTIVE);
        call.setParticipantCount(2);
        when(repository.findByRoomName("call-test1234")).thenReturn(Optional.of(call));
        when(repository.save(call)).thenReturn(call);

        CallService service = new CallService(
                repository,
                mock(AppointmentClient.class),
                mock(LiveKitClient.class),
                mock(AgentDispatcher.class),
                mock(CallEventPublisher.class)
        );
        OffsetDateTime finishedAt = OffsetDateTime.parse("2026-08-04T12:00:00Z");

        service.handleLiveKitEvent("room_finished", "call-test1234", 0, finishedAt);

        assertThat(call.getStatus()).isEqualTo(CallStatus.COMPLETED);
        assertThat(call.getParticipantCount()).isZero();
        assertThat(call.getEndedAt()).isEqualTo(finishedAt);
        verify(repository).save(call);
    }

    @Test
    void shouldPassDocumentedFiltersToRepository() {
        CallRepository repository = mock(CallRepository.class);
        UUID storeId = UUID.randomUUID();
        when(repository.search(CallStatus.ACTIVE, storeId, "90555")).thenReturn(List.of());
        CallService service = new CallService(
                repository,
                mock(AppointmentClient.class),
                mock(LiveKitClient.class),
                mock(AgentDispatcher.class),
                mock(CallEventPublisher.class)
        );

        List<CallResponse> result = service.getCalls(CallStatus.ACTIVE, storeId, " 90555 ");

        assertThat(result).isEmpty();
        verify(repository).search(CallStatus.ACTIVE, storeId, "90555");
    }
}
