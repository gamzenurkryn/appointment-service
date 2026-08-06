package com.gamzenur.callservice.repository;

import com.gamzenur.callservice.domain.Call;
import com.gamzenur.callservice.domain.CallStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CallRepository extends JpaRepository<Call, UUID> {

    List<Call> findAllByStatusOrderByStartedAtDesc(CallStatus status);

    Optional<Call> findByRoomName(String roomName);

    Optional<Call> findFirstByAppointmentId(UUID appointmentId);
}
