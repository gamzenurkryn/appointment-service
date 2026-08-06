package com.gamzenur.crmservice.repository;

import com.gamzenur.crmservice.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {

    boolean existsByAppointmentId(UUID appointmentId);

    List<Offer> findAllByAppointmentIdOrderByCreatedAtDesc(UUID appointmentId);
}
