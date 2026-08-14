package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.AppointmentResponse;
import com.gamzenur.appointmentservice.dto.CreateAppointmentRequest;
import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.AppointmentStatus;
import com.gamzenur.appointmentservice.entity.Channel;
import com.gamzenur.appointmentservice.entity.Store;
import com.gamzenur.appointmentservice.entity.Employee;
import com.gamzenur.appointmentservice.exception.SlotAlreadyBookedException;
import com.gamzenur.appointmentservice.integration.calendar.GoogleCalendarClient;
import com.gamzenur.appointmentservice.messaging.AppointmentEventPublisher;
import com.gamzenur.appointmentservice.mapper.AppointmentMapper;
import com.gamzenur.appointmentservice.repository.AppointmentRepository;
import com.gamzenur.appointmentservice.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private GoogleCalendarClient googleCalendarClient;

    @Mock
    private AppointmentEventPublisher appointmentEventPublisher;

    @Mock
    private EmployeeService employeeService;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository,
                storeRepository,
                new AppointmentMapper(),
                googleCalendarClient,
                appointmentEventPublisher,
                employeeService
        );
    }

    @Test
    void createAppointmentCalculatesEndTimeAndCopiesStoreName() {
        UUID storeId = UUID.randomUUID();
        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1).withSecond(0).withNano(0);
        Store store = store(storeId, "Kadıköy Mağazası");
        CreateAppointmentRequest request = request(storeId, startTime);
        Employee employee = employee(storeId, "Ayşe");

        when(employeeService.findAvailableEmployee(
                eq(storeId),
                isNull(),
                eq("sac-kesimi"),
                eq(startTime),
                eq(startTime.plusMinutes(30)),
                isNull()
        )).thenReturn(employee);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(googleCalendarClient.createEvent(any(Store.class), any(Appointment.class)))
                .thenReturn("calendar-event-123");

        AppointmentResponse response = appointmentService.createAppointment(request);

        assertEquals(startTime.plusMinutes(30), response.getEndTime());
        assertEquals(AppointmentStatus.PENDING, response.getStatus());
        assertEquals("Kadıköy Mağazası", response.getStoreName());
        assertEquals("calendar-event-123", response.getCalendarEventId());
        assertEquals(employee.getId(), response.getEmployeeId());
        assertEquals("Ayşe", response.getEmployeeName());
        verify(appointmentEventPublisher).publishCreated(response);
    }

    @Test
    void createAppointmentUsesServiceSpecificDuration() {
        UUID storeId = UUID.randomUUID();
        OffsetDateTime startTime = OffsetDateTime.now()
                .plusDays(2)
                .withSecond(0)
                .withNano(0);

        Store store = store(storeId, "Nişantaşı Mağazası");
        CreateAppointmentRequest request = request(storeId, startTime);
        request.setServiceType("sac-boyama");
        Employee employee = employee(storeId, "Zeynep");

        when(employeeService.findAvailableEmployee(
                eq(storeId),
                isNull(),
                eq("sac-boyama"),
                eq(startTime),
                eq(startTime.plusMinutes(90)),
                isNull()
        )).thenReturn(employee);

        when(storeRepository.findById(storeId))
                .thenReturn(Optional.of(store));

        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response =
                appointmentService.createAppointment(request);

        assertEquals(
                startTime.plusMinutes(90),
                response.getEndTime()
        );

        assertEquals(employee.getId(), response.getEmployeeId());
        assertEquals("Zeynep", response.getEmployeeName());
    }

    @Test
    void createAppointmentRejectsOverlappingSlot() {
        UUID storeId = UUID.randomUUID();
        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store(storeId, "Demo")));
        when(employeeService.findAvailableEmployee(
                any(),
                isNull(),
                any(),
                any(),
                any(),
                isNull()
        )).thenThrow(
                new SlotAlreadyBookedException(
                        "Bu hizmet ve zaman için uygun çalışan bulunamadı."
                )
        );

        assertThrows(
                SlotAlreadyBookedException.class,
                () -> appointmentService.createAppointment(request(storeId, startTime))
        );
    }

    @Test
    void completedConfirmedCallConfirmsAppointment() {
        UUID appointmentId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, "Nisantasi");
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStoreId(storeId);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCalendarEventId("calendar-event-123");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        String correlationId = "staj-20260810-randevu-002";
        appointmentService.applyCompletedCallResult(appointmentId, "CONFIRMED", correlationId);

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        verify(googleCalendarClient).updateEvent(store, appointment);
        verify(appointmentEventPublisher).publishUpdated(any(AppointmentResponse.class), eq(correlationId));
    }

    private CreateAppointmentRequest request(UUID storeId, OffsetDateTime startTime) {
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setCustomerName("Ayşe Yılmaz");
        request.setCustomerPhone("+905321234567");
        request.setStoreId(storeId);
        request.setServiceType("sac-kesimi");
        request.setStartTime(startTime);
        request.setChannel(Channel.WHATSAPP);
        return request;
    }

    private Store store(UUID id, String name) {
        Store store = new Store();
        store.setId(id);
        store.setName(name);
        store.setLocation("İstanbul");
        store.setPhone("+902121234567");
        store.setTimezone("Europe/Istanbul");
        return store;
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
