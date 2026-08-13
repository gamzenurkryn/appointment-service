package com.gamzenur.notificationservice.controller;

import com.gamzenur.notificationservice.domain.LogLevel;
import com.gamzenur.notificationservice.dto.LogEventResponse;
import com.gamzenur.notificationservice.service.LogEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logs")
@Tag(name = "Merkezi Loglar", description = "Servisler arası olayların merkezi audit kayıtları")
public class LogController {

    private final LogEventService logEventService;

    public LogController(LogEventService logEventService) {
        this.logEventService = logEventService;
    }

    @GetMapping
    @Operation(summary = "Log kayıtlarını listele", description = "Servis, seviye ve correlationId alanlarına göre filtrelenebilir.")
    public Page<LogEventResponse> getLogs(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) LogLevel level,
            @RequestParam(required = false) String correlationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "timestamp")
        );
        return logEventService.getLogs(service, level, correlationId, pageable);
    }
}
