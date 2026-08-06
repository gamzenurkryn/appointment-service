package com.gamzenur.apigateway.controller;

import com.gamzenur.apigateway.dto.SystemStatusResponse;
import com.gamzenur.apigateway.dto.SystemStatusResponse.ServiceStatus;
import com.gamzenur.apigateway.service.SystemStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemStatusControllerTest {

    @Test
    void returnsStatusOfAllDocumentedComponents() {
        SystemStatusService service = mock(SystemStatusService.class);
        SystemStatusResponse response = new SystemStatusResponse(
                ServiceStatus.UP,
                ServiceStatus.UP,
                ServiceStatus.UP,
                ServiceStatus.DOWN
        );
        when(service.getStatus()).thenReturn(Mono.just(response));

        WebTestClient client = WebTestClient
                .bindToController(new SystemStatusController(service))
                .build();

        client.get()
                .uri("/api/v1/system/status")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.dialer").isEqualTo("UP")
                .jsonPath("$.livekitAgent").isEqualTo("UP")
                .jsonPath("$.dashboard").isEqualTo("UP")
                .jsonPath("$.whatsapp").isEqualTo("DOWN");
    }
}
