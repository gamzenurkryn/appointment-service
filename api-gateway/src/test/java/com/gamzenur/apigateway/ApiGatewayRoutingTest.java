package com.gamzenur.apigateway;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayRoutingTest {

    private static final HttpServer APPOINTMENT_SERVICE = startAppointmentService();

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine")
    ).withExposedPorts(6379);

    @LocalServerPort
    private int gatewayPort;

    @DynamicPropertySource
    static void appointmentServiceUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "APPOINTMENT_SERVICE_URL",
                () -> "http://localhost:" + APPOINTMENT_SERVICE.getAddress().getPort()
        );
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @AfterAll
    static void stopAppointmentService() {
        APPOINTMENT_SERVICE.stop(0);
    }

    @Test
    void shouldRouteApiRequestsToAppointmentService() {
        WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + gatewayPort)
                .build()
                .get()
                .uri("/api/v1/stores")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Correlation-Id")
                .expectHeader().contentType("application/json")
                .expectBody()
                .json("[{\"name\":\"Bizim Beauty Saloon - Nişantaşı\"}]");
    }

    @Test
    void shouldPreserveCorrelationId() {
        WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + gatewayPort)
                .build()
                .get()
                .uri("/api/v1/stores")
                .header("X-Correlation-Id", "gateway-test-001")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Correlation-Id", "gateway-test-001");
    }

    private static HttpServer startAppointmentService() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/api/v1/stores", exchange -> {
                byte[] response = "[{\"name\":\"Bizim Beauty Saloon - Nişantaşı\"}]"
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("Test appointment service başlatılamadı.", exception);
        }
    }
}
