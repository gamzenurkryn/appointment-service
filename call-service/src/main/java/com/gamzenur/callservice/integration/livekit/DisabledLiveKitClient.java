package com.gamzenur.callservice.integration.livekit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "integration.livekit", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledLiveKitClient implements LiveKitClient {

    @Override
    public void createRoom(String roomName) {
        // Geliştirme ortamında oda adı ayrılır; gerçek LiveKit çağrısı özellik açılınca yapılır.
    }
}
