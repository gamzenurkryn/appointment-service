package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.AvailabilityResponse;
import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.Employee;
import com.gamzenur.appointmentservice.entity.Store;
import com.gamzenur.appointmentservice.repository.AppointmentRepository;
import com.gamzenur.appointmentservice.repository.EmployeeRepository;
import com.gamzenur.appointmentservice.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.time.LocalTime;
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
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AvailabilityService service = new AvailabilityService(
                appointmentRepository,
                storeRepository,
                employeeRepository
        );
        UUID storeId = UUID.randomUUID();
        Employee employee = employee(storeId, "Ayşe");
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
        when(employeeRepository.findByStoreIdAndActiveTrueAndServiceTypesContainingOrderByName(
                storeId,
                "sac-kesimi"
        )).thenReturn(List.of(employee));

        AvailabilityResponse response = service.getAvailability(storeId, date, "sac-kesimi");

        assertEquals(15, response.getSlots().size());
        assertEquals(9, response.getSlots().getFirst().getStart().getHour());
    }


    @Test
    void getAvailabilityUsesServiceSpecificDuration() {
        AppointmentRepository appointmentRepository =
                mock(AppointmentRepository.class);
        StoreRepository storeRepository =
                mock(StoreRepository.class);
        EmployeeRepository employeeRepository =
                mock(EmployeeRepository.class);

        AvailabilityService service =
                new AvailabilityService(
                        appointmentRepository,
                        storeRepository,
                        employeeRepository
                );

        UUID storeId = UUID.randomUUID();
        ZoneId zoneId = ZoneId.of("Europe/Istanbul");
        LocalDate date = LocalDate.now(zoneId).plusDays(2);

        Store store = new Store();
        store.setId(storeId);
        store.setTimezone("Europe/Istanbul");
        Employee employee = employee(storeId, "Zeynep");

        when(storeRepository.findById(storeId))
                .thenReturn(Optional.of(store));

        when(appointmentRepository.findOverlappingAppointments(
                any(), any(), any()
        )).thenReturn(List.of());
        when(employeeRepository.findByStoreIdAndActiveTrueAndServiceTypesContainingOrderByName(
                storeId,
                "sac-boyama"
        )).thenReturn(List.of(employee));

        AvailabilityResponse response =
                service.getAvailability(
                        storeId,
                        date,
                        "sac-boyama"
                );

        assertEquals(12, response.getSlots().size());
        assertEquals(
                employee.getId(),
                response.getSlots().getFirst().getAvailableEmployees().getFirst().getId()
        );

        assertEquals(
                90,
                java.time.Duration.between(
                        response.getSlots().getFirst().getStart(),
                        response.getSlots().getFirst().getEnd()
                ).toMinutes()
        );
        LocalTime lunchStart = LocalTime.of(13, 0);
        LocalTime lunchEnd = LocalTime.of(14, 0);

        boolean hasSlotOverlappingLunch =
                response.getSlots().stream().anyMatch(slot -> {
                    LocalTime slotStart = slot.getStart()
                            .atZoneSameInstant(zoneId)
                            .toLocalTime();

                    LocalTime slotEnd = slot.getEnd()
                            .atZoneSameInstant(zoneId)
                            .toLocalTime();

                    return slotStart.isBefore(lunchEnd)
                            && slotEnd.isAfter(lunchStart);
                });

        assertFalse(hasSlotOverlappingLunch);
    }

    private Employee employee(UUID storeId, String name) {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setStoreId(storeId);
        employee.setName(name);
        employee.setActive(true);
        return employee;
    }
}
