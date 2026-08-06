package com.gamzenur.apigateway.dto;

public record SystemStatusResponse(
        ServiceStatus dialer,
        ServiceStatus livekitAgent,
        ServiceStatus dashboard,
        ServiceStatus whatsapp
) {
    public enum ServiceStatus {
        UP,
        DOWN
    }
}
