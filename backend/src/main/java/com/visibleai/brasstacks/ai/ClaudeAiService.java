package com.visibleai.brasstacks.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ClaudeAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAiService.class);
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String apiKey;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ClaudeAiService(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<String> decompose(String anonymizedTaskDescription) {
        String prompt = "Break this task into 4-7 concrete micro-steps (each under 2 minutes). "
                + "Return ONLY a JSON array of step title strings, nothing else. "
                + "Task: " + anonymizedTaskDescription;

        String body = String.format(
                "{\"model\":\"claude-haiku-4-5-20251001\",\"max_tokens\":500,\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                escapeJson(prompt));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Claude API error {}: {}", response.statusCode(), response.body());
                return List.of();
            }
            return parseSteps(response.body());
        } catch (Exception e) {
            log.error("Claude API call failed", e);
            return List.of();
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    private List<String> parseSteps(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            String text = root.path("content").get(0).path("text").asText();
            // Extract JSON array from response text
            int start = text.indexOf('[');
            int end = text.lastIndexOf(']');
            if (start >= 0 && end > start) {
                JsonNode array = mapper.readTree(text.substring(start, end + 1));
                List<String> steps = new ArrayList<>();
                for (JsonNode item : array) {
                    steps.add(item.asText());
                }
                return steps;
            }
        } catch (Exception e) {
            log.warn("Failed to parse Claude response", e);
        }
        return List.of();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
