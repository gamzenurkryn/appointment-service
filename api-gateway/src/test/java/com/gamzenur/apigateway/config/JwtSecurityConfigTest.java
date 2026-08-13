package com.gamzenur.apigateway.config;

import com.gamzenur.apigateway.dto.SystemStatusResponse;
import com.gamzenur.apigateway.dto.SystemStatusResponse.ServiceStatus;
import com.gamzenur.apigateway.service.SystemStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.security.jwt.enabled=true",
                "app.security.jwt.secret=test-jwt-secret-with-at-least-32-characters"
        }
)
class JwtSecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SystemStatusService systemStatusService;

    @Test
    void rejectsProtectedApiWithoutBearerToken() {
        webTestClient.get()
                .uri("/api/v1/appointments")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void keepsSystemStatusPublic() {
        when(systemStatusService.getStatus()).thenReturn(Mono.just(new SystemStatusResponse(
                ServiceStatus.UP,
                ServiceStatus.UP,
                ServiceStatus.UP,
                ServiceStatus.UP
        )));

        webTestClient.get()
                .uri("/api/v1/system/status")
                .exchange()
                .expectStatus().isOk();
    }
}
