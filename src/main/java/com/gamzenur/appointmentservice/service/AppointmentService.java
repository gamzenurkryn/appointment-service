package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.AppointmentResponse;
import com.gamzenur.appointmentservice.dto.CreateAppointmentRequest;
import com.gamzenur.appointmentservice.dto.UpdateAppointmentRequest;
import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.AppointmentStatus;
import com.gamzenur.appointmentservice.entity.Store;
import com.gamzenur.appointmentservice.exception.BadRequestException;
import com.gamzenur.appointmentservice.exception.ResourceNotFoundException;
import com.gamzenur.appointmentservice.exception.SlotAlreadyBookedException;
import com.gamzenur.appointmentservice.integration.calendar.GoogleCalendarClient;
import com.gamzenur.appointmentservice.messaging.AppointmentEventPublisher;
import com.gamzenur.appointmentservice.mapper.AppointmentMapper;
import com.gamzenur.appointmentservice.repository.AppointmentRepository;
import com.gamzenur.appointmentservice.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AppointmentService {

    private static final Duration DEFAULT_APPOINTMENT_DURATION = Duration.ofMinutes(30);

    private final AppointmentRepository appointmentRepository;
    private final StoreRepository storeRepository;
    private final AppointmentMapper appointmentMapper;
    private final GoogleCalendarClient googleCalendarClient;
    private final AppointmentEventPublisher appointmentEventPublisher;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            StoreRepository storeRepository,
            AppointmentMapper appointmentMapper,
            GoogleCalendarClient googleCalendarClient,
            AppointmentEventPublisher appointmentEventPublisher
    ) {
        this.appointmentRepository = appointmentRepository;
        this.storeRepository = storeRepository;
        this.appointmentMapper = appointmentMapper;
        this.googleCalendarClient = googleCalendarClient;
        this.appointmentEventPublisher = appointmentEventPublisher;
    }

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        ServiceCatalog.validate(request.getServiceType());
        Store store = findStore(request.getStoreId());
        OffsetDateTime endTime = request.getStartTime().plus(DEFAULT_APPOINTMENT_DURATION);
        validateFutureTime(request.getStartTime());
        ensureSlotIsAvailable(request.getStoreId(), request.getStartTime(), endTime, null);

        Appointment appointment = appointmentMapper.toEntity(request);
        OffsetDateTime now = OffsetDateTime.now();
        appointment.setStoreName(store.getName());
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        appointment.setCalendarEventId(googleCalendarClient.createEvent(store, appointment));

        AppointmentResponse response = appointmentMapper.toResponse(saveWithoutOverlap(appointment));
        appointmentEventPublisher.publishCreated(response);
        return response;
    }

    public Page<AppointmentResponse> getAppointments(
            UUID storeId,
            String customerPhone,
            AppointmentStatus status,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        Specification<Appointment> specification = (root, query, builder) -> builder.conjunction();

        if (storeId != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("storeId"), storeId));
        }
        if (customerPhone != null && !customerPhone.isBlank()) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("customerPhone"), customerPhone));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status"), status));
        }
        if (from != null) {
            specification = specification.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("startTime"), from));
        }
        if (to != null) {
            specification = specification.and((root, query, builder) ->
                    builder.lessThan(root.get("startTime"), to));
        }

        return appointmentRepository.findAll(specification, pageable)
                .map(appointmentMapper::toResponse);
    }

    public AppointmentResponse getAppointmentById(UUID id) {
        return appointmentMapper.toResponse(findAppointment(id));
    }

    @Transactional
    public void deleteAppointment(UUID id) {
        Appointment appointment = findAppointment(id);
        Store store = findStore(appointment.getStoreId());
        if (appointment.getCalendarEventId() != null) {
            googleCalendarClient.deleteEvent(store, appointment.getCalendarEventId());
            appointment.setCalendarEventId(null);
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(OffsetDateTime.now());
        AppointmentResponse response = appointmentMapper.toResponse(appointmentRepository.save(appointment));
        appointmentEventPublisher.publishCancelled(response);
    }

    @Transactional
    public AppointmentResponse updateAppointment(UUID id, UpdateAppointmentRequest request) {
        Appointment appointment = findAppointment(id);
        Store store = findStore(appointment.getStoreId());

        if (request.getStartTime() != null) {
            OffsetDateTime endTime = request.getStartTime().plus(DEFAULT_APPOINTMENT_DURATION);
            validateFutureTime(request.getStartTime());
            ensureSlotIsAvailable(appointment.getStoreId(), request.getStartTime(), endTime, id);
            appointment.setStartTime(request.getStartTime());
            appointment.setEndTime(endTime);
        }
        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        appointment.setUpdatedAt(OffsetDateTime.now());
        synchronizeCalendarEvent(store, appointment);
        AppointmentResponse response = appointmentMapper.toResponse(saveWithoutOverlap(appointment));
        appointmentEventPublisher.publishUpdated(response);
        return response;
    }

    @Transactional
    public void applyCompletedCallResult(UUID appointmentId, String callResult) {
        applyCompletedCallResult(appointmentId, callResult, null);
    }

    @Transactional
    public void applyCompletedCallResult(UUID appointmentId, String callResult, String correlationId) {
        AppointmentStatus targetStatus = switch (callResult) {
            case "CONFIRMED" -> AppointmentStatus.CONFIRMED;
            case "DECLINED" -> AppointmentStatus.CANCELLED;
            case "RESCHEDULE_REQUESTED" -> AppointmentStatus.RESCHEDULED;
            default -> null;
        };
        if (targetStatus == null) {
            return;
        }

        Appointment appointment = findAppointment(appointmentId);
        if (appointment.getStatus() == targetStatus) {
            return;
        }

        Store store = findStore(appointment.getStoreId());
        appointment.setStatus(targetStatus);
        appointment.setUpdatedAt(OffsetDateTime.now());
        synchronizeCalendarEvent(store, appointment);
        AppointmentResponse response = appointmentMapper.toResponse(appointmentRepository.save(appointment));
        if (targetStatus == AppointmentStatus.CANCELLED) {
            appointmentEventPublisher.publishCancelled(response, correlationId);
        } else {
            appointmentEventPublisher.publishUpdated(response, correlationId);
        }
    }

    private Appointment findAppointment(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı: " + id));
    }

    private Store findStore(UUID id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mağaza bulunamadı: " + id));
    }

    private void validateFutureTime(OffsetDateTime startTime) {
        if (!startTime.isAfter(OffsetDateTime.now())) {
            throw new BadRequestException("Randevu başlangıç zamanı gelecekte olmalıdır.");
        }
    }

    private void ensureSlotIsAvailable(
            UUID storeId,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            UUID excludedAppointmentId
    ) {
        boolean overlaps = appointmentRepository.existsOverlappingAppointment(
                storeId,
                startTime,
                endTime,
                AppointmentStatus.CANCELLED,
                excludedAppointmentId
        );
        if (overlaps) {
            throw new SlotAlreadyBookedException("Seçilen zaman dilimi az önce doldu.");
        }
    }

    private Appointment saveWithoutOverlap(Appointment appointment) {
        try {
            return appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException exception) {
            throw new SlotAlreadyBookedException("Seçilen zaman dilimi az önce doldu.");
        }
    }

    private void synchronizeCalendarEvent(Store store, Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            if (appointment.getCalendarEventId() != null) {
                googleCalendarClient.deleteEvent(store, appointment.getCalendarEventId());
                appointment.setCalendarEventId(null);
            }
            return;
        }

        if (appointment.getCalendarEventId() == null) {
            appointment.setCalendarEventId(googleCalendarClient.createEvent(store, appointment));
        } else {
            googleCalendarClient.updateEvent(store, appointment);
        }
    }
}
