package com.gamzenur.appointmentservice.repository;

import com.gamzenur.appointmentservice.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
}
