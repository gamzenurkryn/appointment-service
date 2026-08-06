package com.gamzenur.appointmentservice.repository;

import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID>,
        JpaSpecificationExecutor<Appointment> {

    List<Appointment> findByStoreIdAndStartTimeBetween(
            UUID storeId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    @Query("""
            select a from Appointment a
            where a.storeId = :storeId
              and a.status <> com.gamzenur.appointmentservice.entity.AppointmentStatus.CANCELLED
              and a.startTime < :endTime
              and a.endTime > :startTime
            """)
    List<Appointment> findOverlappingAppointments(
            @Param("storeId") UUID storeId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime
    );

    @Query("""
            select count(a) > 0 from Appointment a
            where a.storeId = :storeId
              and a.status <> :cancelledStatus
              and (:excludedId is null or a.id <> :excludedId)
              and a.startTime < :endTime
              and a.endTime > :startTime
            """)
    boolean existsOverlappingAppointment(
            @Param("storeId") UUID storeId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus,
            @Param("excludedId") UUID excludedId
    );
}
