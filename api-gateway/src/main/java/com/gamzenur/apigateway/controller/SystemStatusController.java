package com.gamzenur.apigateway.controller;

import com.gamzenur.apigateway.dto.SystemStatusResponse;
import com.gamzenur.apigateway.service.SystemStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/system")
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    public SystemStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping("/status")
    public Mono<SystemStatusResponse> getStatus() {
        return systemStatusService.getStatus();
    }
}
