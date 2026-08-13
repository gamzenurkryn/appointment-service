package com.gamzenur.appointmentservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentApiIntegrationTest {

    private static final String STORE_ID = "b2c30000-0000-0000-0000-000000000001";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("agenticai_test")
            .withUsername("postgres")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void appointmentLifecycleAndDoubleBookingProtectionWork() throws Exception {
        String requestBody = """
                {
                  "customerName": "Ayşe Yılmaz",
                  "customerPhone": "+905321234567",
                  "storeId": "%s",
                  "serviceType": "sac-kesimi",
                  "startTime": "2030-08-03T10:00:00+03:00",
                  "channel": "WHATSAPP"
                }
                """.formatted(STORE_ID);

        String responseBody = mockMvc.perform(post("/api/v1/appointments")
                        .header("X-Correlation-Id", "integration-test-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.storeName").value("Bizim Beauty Saloon - Nişantaşı"))
                .andExpect(jsonPath("$.endTime").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdAppointment = objectMapper.readTree(responseBody);
        String appointmentId = createdAppointment.get("id").asText();

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SLOT_ALREADY_BOOKED"));

        mockMvc.perform(delete("/api/v1/appointments/{id}", appointmentId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/appointments/{id}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/v1/availability")
                        .param("storeId", STORE_ID)
                        .param("date", "2030-08-03")
                        .param("serviceType", "sac-kesimi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots[?(@.start == '2030-08-03T10:00:00+03:00')]").exists());
    }
}
