package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.AvailabilityResponse;
import com.gamzenur.appointmentservice.dto.TimeSlotResponse;
import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.Store;
import com.gamzenur.appointmentservice.exception.BadRequestException;
import com.gamzenur.appointmentservice.exception.ResourceNotFoundException;
import com.gamzenur.appointmentservice.repository.AppointmentRepository;
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
    private static final Duration SLOT_DURATION = Duration.ofMinutes(30);

    private final AppointmentRepository appointmentRepository;
    private final StoreRepository storeRepository;

    public AvailabilityService(
            AppointmentRepository appointmentRepository,
            StoreRepository storeRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.storeRepository = storeRepository;
    }

    public AvailabilityResponse getAvailability(UUID storeId, LocalDate date, String serviceType) {
        if (serviceType == null || serviceType.isBlank()) {
            throw new BadRequestException("serviceType boş olamaz.");
        }
        ServiceCatalog.validate(serviceType);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mağaza bulunamadı: " + storeId));
        ZoneId zoneId = parseZoneId(store.getTimezone());
        ZonedDateTime opening = date.atTime(OPENING_TIME).atZone(zoneId);
        ZonedDateTime closing = date.atTime(CLOSING_TIME).atZone(zoneId);

        List<Appointment> appointments = appointmentRepository.findOverlappingAppointments(
                storeId,
                opening.toOffsetDateTime(),
                closing.toOffsetDateTime()
        );

        List<TimeSlotResponse> slots = new ArrayList<>();
        ZonedDateTime cursor = opening;
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        while (!cursor.plus(SLOT_DURATION).isAfter(closing)) {
            OffsetDateTime slotStart = cursor.toOffsetDateTime();
            OffsetDateTime slotEnd = cursor.plus(SLOT_DURATION).toOffsetDateTime();
            boolean isOccupied = appointments.stream().anyMatch(appointment ->
                    appointment.getStartTime().isBefore(slotEnd)
                            && appointment.getEndTime().isAfter(slotStart));

            if (cursor.isAfter(now) && !isOccupied) {
                slots.add(new TimeSlotResponse(slotStart, slotEnd));
            }
            cursor = cursor.plus(SLOT_DURATION);
        }

        AvailabilityResponse response = new AvailabilityResponse();
        response.setStoreId(storeId);
        response.setDate(date);
        response.setSlots(slots);
        return response;
    }

    private ZoneId parseZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            throw new BadRequestException("Mağazanın timezone değeri geçersiz: " + timezone);
        }
    }
}
