package com.gamzenur.appointmentservice.mapper;

import com.gamzenur.appointmentservice.dto.AppointmentResponse;
import com.gamzenur.appointmentservice.dto.CreateAppointmentRequest;
import com.gamzenur.appointmentservice.entity.Appointment;
import org.springframework.stereotype.Component;


@Component
public class AppointmentMapper {

    public Appointment toEntity(CreateAppointmentRequest request ){

        Appointment appointment = new Appointment();

        appointment.setCustomerName(request.getCustomerName());
        appointment.setCustomerPhone(request.getCustomerPhone());
        appointment.setStoreId(request.getStoreId());
        appointment.setServiceType(request.getServiceType());
        appointment.setStartTime(request.getStartTime());
        appointment.setChannel(request.getChannel());

        return appointment;
    }

    public AppointmentResponse toResponse(Appointment appointment){

        AppointmentResponse response = new AppointmentResponse();

        response.setId(appointment.getId());
        response.setCustomerName(appointment.getCustomerName());
        response.setCustomerPhone(appointment.getCustomerPhone());
        response.setStoreId(appointment.getStoreId());
        response.setStoreName(appointment.getStoreName());
        response.setServiceType(appointment.getServiceType());
        response.setStartTime(appointment.getStartTime());
        response.setEndTime(appointment.getEndTime());
        response.setStatus(appointment.getStatus());
        response.setChannel(appointment.getChannel());
        response.setCalendarEventId(appointment.getCalendarEventId());
        response.setNotes(appointment.getNotes());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());

        return response;
    }

}