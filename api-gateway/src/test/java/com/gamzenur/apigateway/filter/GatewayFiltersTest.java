package com.gamzenur.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayFiltersTest {

    @Test
    void shouldAddCorrelationIdToRequestAndResponse() {
        CorrelationIdGlobalFilter filter = new CorrelationIdGlobalFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/stores"));

        GatewayFilterChain chain = filteredExchange -> {
            assertThat(filteredExchange.getRequest().getHeaders()
                    .getFirst(CorrelationIdGlobalFilter.HEADER_NAME)).isNotBlank();
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.HEADER_NAME)).isNotBlank();
    }

    @Test
    void shouldReturnStandardErrorWhenDownstreamConnectionFails() {
        CorrelationIdGlobalFilter correlationFilter = new CorrelationIdGlobalFilter();
        GatewayUnavailableFilter unavailableFilter = new GatewayUnavailableFilter(
                new ObjectMapper().findAndRegisterModules()
        );
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/stores"));
        GatewayFilterChain failingChain = ignored -> Mono.error(new ConnectException("Connection refused"));

        StepVerifier.create(correlationFilter.filter(
                exchange,
                correlatedExchange -> unavailableFilter.filter(correlatedExchange, failingChain)
        )).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(503);
        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("SERVICE_UNAVAILABLE");
        assertThat(body).contains("correlationId");
    }
}
