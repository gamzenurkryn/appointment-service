package com.gamzenur.callservice.integration.livekit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(
        prefix = "integration.livekit",
        name = "agent-dispatch-enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class DisabledAgentDispatcher implements AgentDispatcher {

    @Override
    public void dispatch(String roomName, UUID callId, UUID appointmentId) {
        // AI ajanı hazır olana kadar oda, ajan gönderilmeden oluşturulur.
    }
}
