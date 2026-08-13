package com.gamzenur.callservice.integration.livekit;

import com.gamzenur.callservice.exception.LiveKitIntegrationException;
import io.livekit.server.RoomServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import retrofit2.Response;

import java.io.IOException;

@Component
@ConditionalOnProperty(prefix = "integration.livekit", name = "enabled", havingValue = "true")
public class LiveKitRoomClient implements LiveKitClient {

    private final String url;
    private final String apiKey;
    private final String apiSecret;

    public LiveKitRoomClient(
            @Value("${integration.livekit.url:}") String url,
            @Value("${integration.livekit.api-key:}") String apiKey,
            @Value("${integration.livekit.api-secret:}") String apiSecret
    ) {
        this.url = url;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public void createRoom(String roomName) {
        validateConfiguration();
        RoomServiceClient client = RoomServiceClient.createClient(normalizeUrl(url), apiKey, apiSecret);

        try {
            Response<?> response = client.createRoom(roomName).execute();
            if (!response.isSuccessful()) {
                throw new LiveKitIntegrationException(
                        "LiveKit oda oluşturma isteğini reddetti. HTTP durum: " + response.code()
                );
            }
        } catch (IOException exception) {
            throw new LiveKitIntegrationException(
                    "LiveKit ile bağlantı kurulamadı: " + safeCause(exception),
                    exception
            );
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(url)
                || !StringUtils.hasText(apiKey)
                || !StringUtils.hasText(apiSecret)) {
            throw new LiveKitIntegrationException(
                    "LiveKit ayarları eksik. LIVEKIT_URL, LIVEKIT_API_KEY ve LIVEKIT_API_SECRET gereklidir."
            );
        }
    }

    private String normalizeUrl(String value) {
        String normalized = value.trim()
                .replaceFirst("^wss://", "https://")
                .replaceFirst("^ws://", "http://");
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }

    private String safeCause(IOException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}
