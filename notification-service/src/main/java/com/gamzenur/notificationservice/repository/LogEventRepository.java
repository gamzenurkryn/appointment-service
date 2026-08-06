package com.gamzenur.notificationservice.repository;

import com.gamzenur.notificationservice.domain.LogEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LogEventRepository extends JpaRepository<LogEvent, UUID>, JpaSpecificationExecutor<LogEvent> {
}
