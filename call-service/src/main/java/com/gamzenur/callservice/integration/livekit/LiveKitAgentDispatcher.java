package com.gamzenur.callservice.integration.livekit;

import com.gamzenur.callservice.exception.LiveKitIntegrationException;
import io.livekit.server.AgentDispatchServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import retrofit2.Response;

import java.io.IOException;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        prefix = "integration.livekit",
        name = "agent-dispatch-enabled",
        havingValue = "true"
)
public class LiveKitAgentDispatcher implements AgentDispatcher {

    private final String url;
    private final String apiKey;
    private final String apiSecret;
    private final String agentName;

    public LiveKitAgentDispatcher(
            @Value("${integration.livekit.url:}") String url,
            @Value("${integration.livekit.api-key:}") String apiKey,
            @Value("${integration.livekit.api-secret:}") String apiSecret,
            @Value("${integration.livekit.agent-name:}") String agentName
    ) {
        this.url = url;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.agentName = agentName;
    }

    @Override
    public void dispatch(String roomName, UUID callId, UUID appointmentId) {
        validateConfiguration();
        AgentDispatchServiceClient client = AgentDispatchServiceClient.createClient(
                normalizeUrl(url),
                apiKey,
                apiSecret
        );
        String metadata = "{\"callId\":\"" + callId
                + "\",\"appointmentId\":\"" + appointmentId + "\"}";

        try {
            Response<?> response = client.createDispatch(roomName, agentName, metadata).execute();
            if (!response.isSuccessful()) {
                throw new LiveKitIntegrationException(
                        "LiveKit AI ajan yönlendirme isteğini reddetti. HTTP durum: " + response.code()
                );
            }
        } catch (IOException exception) {
            throw new LiveKitIntegrationException("LiveKit AI ajanına bağlanılamadı.", exception);
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(url)
                || !StringUtils.hasText(apiKey)
                || !StringUtils.hasText(apiSecret)
                || !StringUtils.hasText(agentName)) {
            throw new LiveKitIntegrationException(
                    "AI ajan ayarları eksik. LIVEKIT_URL, LIVEKIT_API_KEY, "
                            + "LIVEKIT_API_SECRET ve LIVEKIT_AGENT_NAME gereklidir."
            );
        }
    }

    private String normalizeUrl(String value) {
        String normalized = value.trim()
                .replaceFirst("^wss://", "https://")
                .replaceFirst("^ws://", "http://");
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }
}
