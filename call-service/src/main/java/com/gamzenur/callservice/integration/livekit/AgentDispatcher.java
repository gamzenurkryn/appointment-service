package com.gamzenur.callservice.integration.livekit;

import java.util.UUID;

public interface AgentDispatcher {

    void dispatch(String roomName, UUID callId, UUID appointmentId);
}
