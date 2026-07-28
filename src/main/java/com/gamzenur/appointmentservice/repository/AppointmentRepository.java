package com.gamzenur.appointmentservice.repository;

import com.gamzenur.appointmentservice.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppointmentRepository  extends JpaRepository<Appointment, UUID>{
}
