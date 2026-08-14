package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.CreateEmployeeRequest;
import com.gamzenur.appointmentservice.dto.EmployeeResponse;
import com.gamzenur.appointmentservice.entity.Employee;
import com.gamzenur.appointmentservice.exception.ResourceNotFoundException;
import com.gamzenur.appointmentservice.repository.EmployeeRepository;
import com.gamzenur.appointmentservice.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gamzenur.appointmentservice.entity.AppointmentStatus;
import com.gamzenur.appointmentservice.exception.BadRequestException;
import com.gamzenur.appointmentservice.exception.SlotAlreadyBookedException;
import com.gamzenur.appointmentservice.repository.AppointmentRepository;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final AppointmentRepository appointmentRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            StoreRepository storeRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        ensureStoreExists(request.getStoreId());
        validateServiceTypes(request.getServiceTypes());

        Employee employee = new Employee();
        employee.setStoreId(request.getStoreId());
        employee.setName(request.getName().trim());
        employee.setActive(true);
        employee.setServiceTypes(
                new LinkedHashSet<>(request.getServiceTypes())
        );

        return toResponse(employeeRepository.save(employee));
    }

    public List<EmployeeResponse> getEmployees(
            UUID storeId,
            String serviceType
    ) {
        ensureStoreExists(storeId);

        List<Employee> employees;

        if (serviceType == null || serviceType.isBlank()) {
            employees =
                    employeeRepository.findByStoreIdAndActiveTrueOrderByName(
                            storeId
                    );
        } else {
            ServiceCatalog.validate(serviceType);

            employees =
                    employeeRepository
                            .findByStoreIdAndActiveTrueAndServiceTypesContainingOrderByName(
                                    storeId,
                                    serviceType
                            );
        }

        return employees.stream()
                .map(this::toResponse)
                .toList();
    }

    public Employee findAvailableEmployee(
            UUID storeId,
            UUID requestedEmployeeId,
            String serviceType,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            UUID excludedAppointmentId
    ) {
        ServiceCatalog.validate(serviceType);

        if (requestedEmployeeId != null) {
            Employee employee = employeeRepository.findById(requestedEmployeeId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Çalışan bulunamadı: " + requestedEmployeeId
                    ));

            validateEmployee(employee, storeId, serviceType);

            if (hasOverlap(employee.getId(), startTime, endTime, excludedAppointmentId)) {
                throw new SlotAlreadyBookedException(
                        "Seçilen çalışan bu zaman aralığında uygun değil."
                );
            }

            return employee;
        }

        List<Employee> employees = employeeRepository
                .findByStoreIdAndActiveTrueAndServiceTypesContainingOrderByName(
                        storeId,
                        serviceType
                );

        return employees.stream()
                .filter(employee -> !hasOverlap(
                        employee.getId(),
                        startTime,
                        endTime,
                        excludedAppointmentId
                ))
                .findFirst()
                .orElseThrow(() -> new SlotAlreadyBookedException(
                        "Bu hizmet ve zaman için uygun çalışan bulunamadı."
                ));
    }

    private void validateEmployee(Employee employee, UUID storeId, String serviceType) {
        if (!employee.isActive()) {
            throw new BadRequestException("Seçilen çalışan aktif değil.");
        }
        if (!employee.getStoreId().equals(storeId)) {
            throw new BadRequestException("Seçilen çalışan bu şubeye ait değil.");
        }
        if (!employee.getServiceTypes().contains(serviceType)) {
            throw new BadRequestException("Seçilen çalışan bu hizmeti vermiyor.");
        }
    }

    private boolean hasOverlap(
            UUID employeeId,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            UUID excludedAppointmentId
    ) {
        return appointmentRepository.existsOverlappingEmployeeAppointment(
                employeeId,
                startTime,
                endTime,
                AppointmentStatus.CANCELLED,
                excludedAppointmentId
        );
    }

    private void ensureStoreExists(UUID storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException(
                    "Mağaza bulunamadı: " + storeId
            );
        }
    }

    private void validateServiceTypes(Set<String> serviceTypes) {
        for (String serviceType : serviceTypes) {
            ServiceCatalog.validate(serviceType);
        }
    }

    private EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setStoreId(employee.getStoreId());
        response.setName(employee.getName());
        response.setActive(employee.isActive());
        response.setServiceTypes(
                new LinkedHashSet<>(employee.getServiceTypes())
        );
        return response;
    }

}
