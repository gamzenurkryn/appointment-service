package com.gamzenur.apigateway.service;

import com.gamzenur.apigateway.dto.SystemStatusResponse;
import com.gamzenur.apigateway.dto.SystemStatusResponse.ServiceStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class SystemStatusService {

    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(2);

    private final WebClient webClient;
    private final String callServiceHealthUrl;
    private final String notificationServiceHealthUrl;
    private final String whatsappServiceHealthUrl;

    public SystemStatusService(
            WebClient.Builder webClientBuilder,
            @Value("${system-status.call-service-health-url:http://localhost:8082/actuator/health}")
            String callServiceHealthUrl,
            @Value("${system-status.notification-service-health-url:http://localhost:8083/actuator/health}")
            String notificationServiceHealthUrl,
            @Value("${system-status.whatsapp-service-health-url:http://localhost:8081/actuator/health}")
            String whatsappServiceHealthUrl
    ) {
        this.webClient = webClientBuilder.build();
        this.callServiceHealthUrl = callServiceHealthUrl;
        this.notificationServiceHealthUrl = notificationServiceHealthUrl;
        this.whatsappServiceHealthUrl = whatsappServiceHealthUrl;
    }

    public Mono<SystemStatusResponse> getStatus() {
        Mono<ServiceStatus> callService = check(callServiceHealthUrl).cache();
        Mono<ServiceStatus> dashboard = check(notificationServiceHealthUrl);
        Mono<ServiceStatus> whatsapp = check(whatsappServiceHealthUrl);

        return Mono.zip(callService, dashboard, whatsapp)
                .map(statuses -> new SystemStatusResponse(
                        statuses.getT1(),
                        statuses.getT1(),
                        statuses.getT2(),
                        statuses.getT3()
                ));
    }

    private Mono<ServiceStatus> check(String healthUrl) {
        return webClient.get()
                .uri(healthUrl)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> Mono.error(new IllegalStateException("Health check failed")))
                .bodyToMono(HealthResponse.class)
                .map(response -> "UP".equalsIgnoreCase(response.status()) ? ServiceStatus.UP : ServiceStatus.DOWN)
                .timeout(HEALTH_CHECK_TIMEOUT)
                .onErrorReturn(ServiceStatus.DOWN);
    }

    private record HealthResponse(String status) {
    }
}
