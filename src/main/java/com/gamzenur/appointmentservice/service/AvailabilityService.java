package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.AvailabilityResponse;
import com.gamzenur.appointmentservice.dto.AvailableEmployeeResponse;
import com.gamzenur.appointmentservice.dto.TimeSlotResponse;
import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.Employee;
import com.gamzenur.appointmentservice.entity.Store;
import com.gamzenur.appointmentservice.exception.BadRequestException;
import com.gamzenur.appointmentservice.exception.ResourceNotFoundException;
import com.gamzenur.appointmentservice.repository.AppointmentRepository;
import com.gamzenur.appointmentservice.repository.EmployeeRepository;
import com.gamzenur.appointmentservice.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    private static final LocalTime OPENING_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);
    private static final Duration SLOT_INTERVAL = Duration.ofMinutes(30);
    private static final LocalTime LUNCH_START = LocalTime.of(13, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(14, 0);

    private final AppointmentRepository appointmentRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;

    public AvailabilityService(
            AppointmentRepository appointmentRepository,
            StoreRepository storeRepository,
            EmployeeRepository employeeRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.storeRepository = storeRepository;
        this.employeeRepository = employeeRepository;
    }

    public AvailabilityResponse getAvailability(UUID storeId, LocalDate date, String serviceType) {
        if (serviceType == null || serviceType.isBlank()) {
            throw new BadRequestException("serviceType boş olamaz.");
        }
        ServiceCatalog.validate(serviceType);
        Duration serviceDuration = ServiceCatalog.duration(serviceType);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mağaza bulunamadı: " + storeId));
        ZoneId zoneId = parseZoneId(store.getTimezone());
        ZonedDateTime opening = date.atTime(OPENING_TIME).atZone(zoneId);
        ZonedDateTime closing = date.atTime(CLOSING_TIME).atZone(zoneId);
        ZonedDateTime lunchStart = date.atTime(LUNCH_START).atZone(zoneId);
        ZonedDateTime lunchEnd = date.atTime(LUNCH_END).atZone(zoneId);

        List<Appointment> appointments = appointmentRepository.findOverlappingAppointments(
                storeId,
                opening.toOffsetDateTime(),
                closing.toOffsetDateTime()
        );
        List<Employee> eligibleEmployees = employeeRepository
                .findByStoreIdAndActiveTrueAndServiceTypesContainingOrderByName(
                        storeId,
                        serviceType
                );

        List<TimeSlotResponse> slots = new ArrayList<>();
        ZonedDateTime cursor = opening;
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        while (!cursor.plus(serviceDuration).isAfter(closing)) {
            OffsetDateTime slotStart = cursor.toOffsetDateTime();
            OffsetDateTime slotEnd = cursor.plus(serviceDuration).toOffsetDateTime();
            boolean overlapsLunch =
                    cursor.isBefore(lunchEnd)
                            && cursor.plus(serviceDuration).isAfter(lunchStart);

            List<AvailableEmployeeResponse> availableEmployees = eligibleEmployees.stream()
                    .filter(employee -> isEmployeeAvailable(
                            employee,
                            appointments,
                            slotStart,
                            slotEnd
                    ))
                    .map(employee -> new AvailableEmployeeResponse(
                            employee.getId(),
                            employee.getName()
                    ))
                    .toList();

            if (cursor.isAfter(now) && !overlapsLunch && !availableEmployees.isEmpty()) {
                slots.add(new TimeSlotResponse(slotStart, slotEnd, availableEmployees));
            }
            cursor = cursor.plus(SLOT_INTERVAL);
        }

        AvailabilityResponse response = new AvailabilityResponse();
        response.setStoreId(storeId);
        response.setDate(date);
        response.setSlots(slots);
        return response;
    }

    private boolean isEmployeeAvailable(
            Employee employee,
            List<Appointment> appointments,
            OffsetDateTime slotStart,
            OffsetDateTime slotEnd
    ) {
        return appointments.stream().noneMatch(appointment -> {
            boolean overlaps = appointment.getStartTime().isBefore(slotEnd)
                    && appointment.getEndTime().isAfter(slotStart);
            boolean blocksEmployee = appointment.getEmployeeId() == null
                    || appointment.getEmployeeId().equals(employee.getId());
            return overlaps && blocksEmployee;
        });
    }

    private ZoneId parseZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            throw new BadRequestException("Mağazanın timezone değeri geçersiz: " + timezone);
        }
    }
}
