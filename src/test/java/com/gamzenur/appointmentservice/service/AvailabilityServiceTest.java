package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.AvailabilityResponse;
import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.Store;
import com.gamzenur.appointmentservice.repository.AppointmentRepository;
import com.gamzenur.appointmentservice.repository.StoreRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AvailabilityServiceTest {

    @Test
    void getAvailabilityExcludesOccupiedSlot() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        StoreRepository storeRepository = mock(StoreRepository.class);
        AvailabilityService service = new AvailabilityService(appointmentRepository, storeRepository);
        UUID storeId = UUID.randomUUID();
        LocalDate date = LocalDate.now(ZoneId.of("Europe/Istanbul")).plusDays(2);

        Store store = new Store();
        store.setId(storeId);
        store.setTimezone("Europe/Istanbul");

        ZonedDateTime occupiedStart = date.atTime(10, 0).atZone(ZoneId.of("Europe/Istanbul"));
        Appointment occupied = new Appointment();
        occupied.setStartTime(occupiedStart.toOffsetDateTime());
        occupied.setEndTime(occupiedStart.plusMinutes(30).toOffsetDateTime());

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any()))
                .thenReturn(List.of(occupied));

        AvailabilityResponse response = service.getAvailability(storeId, date, "sac-kesimi");

        assertEquals(17, response.getSlots().size());
        assertEquals(9, response.getSlots().getFirst().getStart().getHour());
    }
}
