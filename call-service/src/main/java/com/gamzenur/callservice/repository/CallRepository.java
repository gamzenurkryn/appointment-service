package com.gamzenur.callservice.repository;

import com.gamzenur.callservice.domain.Call;
import com.gamzenur.callservice.domain.CallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CallRepository extends JpaRepository<Call, UUID> {

    List<Call> findAllByStatusOrderByStartedAtDesc(CallStatus status);

    @Query("""
            select call from Call call
            where (:status is null or call.status = :status)
              and (:storeId is null or call.storeId = :storeId)
              and (:query is null
                   or lower(call.roomName) like lower(concat('%', :query, '%'))
                   or call.customerPhone like concat('%', :query, '%'))
            order by call.startedAt desc
            """)
    List<Call> search(
            @Param("status") CallStatus status,
            @Param("storeId") UUID storeId,
            @Param("query") String query
    );

    Optional<Call> findByRoomName(String roomName);

    Optional<Call> findFirstByAppointmentId(UUID appointmentId);
}
