package com.visibleai.brasstacks.notification;

import com.visibleai.brasstacks.model.PushToken;
import com.visibleai.brasstacks.repository.PushTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final PushTokenRepository pushTokenRepository;
    private final HttpClient httpClient;

    public NotificationService(PushTokenRepository pushTokenRepository) {
        this.pushTokenRepository = pushTokenRepository;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void sendToUser(Long userId, String title, String body) {
        List<PushToken> tokens = pushTokenRepository.findByUserId(userId);
        for (PushToken pt : tokens) {
            sendPush(pt.getToken(), title, body);
        }
    }

    private void sendPush(String expoPushToken, String title, String body) {
        String json = String.format(
                "{\"to\":\"%s\",\"title\":\"%s\",\"body\":\"%s\",\"sound\":\"default\"}",
                escapeJson(expoPushToken),
                escapeJson(title),
                escapeJson(body)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EXPO_PUSH_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Expo push failed for token {}: {}", expoPushToken, response.body());
            }
        } catch (Exception e) {
            log.error("Failed to send push notification", e);
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
