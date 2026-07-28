package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.AppointmentResponse;
import com.gamzenur.appointmentservice.dto.CreateAppointmentRequest;
import com.gamzenur.appointmentservice.entity.Appointment;
import com.gamzenur.appointmentservice.entity.AppointmentStatus;
import com.gamzenur.appointmentservice.mapper.AppointmentMapper;
import com.gamzenur.appointmentservice.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;


@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
    }

    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {

        Appointment appointment = appointmentMapper.toEntity(request);

        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(OffsetDateTime.now());
        appointment.setUpdatedAt(OffsetDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toResponse(savedAppointment);
    }
}
