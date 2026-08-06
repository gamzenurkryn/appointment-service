package com.gamzenur.notificationservice.websocket;

public record CallStats(
        long activeCalls,
        long participants,
        long matched
) {
}
