package com.gamzenur.appointmentservice.repository;

import com.gamzenur.appointmentservice.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    List<Employee> findByStoreIdAndActiveTrueOrderByName(UUID storeId);

    List<Employee> findByStoreIdAndActiveTrueAndServiceTypesContainingOrderByName(
            UUID storeId,
            String serviceType
    );
}