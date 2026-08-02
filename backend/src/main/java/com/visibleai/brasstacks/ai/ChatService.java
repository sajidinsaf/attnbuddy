package com.visibleai.brasstacks.ai;

import com.visibleai.brasstacks.model.*;
import com.visibleai.brasstacks.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final LifeDomainRepository domainRepository;

    public ChatService(TaskRepository taskRepository, GoalRepository goalRepository,
                       LifeDomainRepository domainRepository) {
        this.taskRepository = taskRepository;
        this.goalRepository = goalRepository;
        this.domainRepository = domainRepository;
    }

    public String chat(User user, String apiKey, List<ChatMessage> history, String userMessage) {
        String systemPrompt = buildSystemPrompt(user);
        String response = callAi(user, apiKey, systemPrompt, history, userMessage);
        return response != null ? response : "I couldn't process that right now. Please try again.";
    }

    private String buildSystemPrompt(User user) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are the Brasstacks assistant — a helpful, ").append(toneDescription(user.getTone()))
          .append(" companion inside a focus and productivity app.\n\n");

        sb.append("## About Brasstacks\n");
        sb.append("Brasstacks shows ONE task at a time (the Right Now screen) instead of overwhelming lists. ");
        sb.append("It uses smart prioritization (Eisenhower matrix + deadlines + learned patterns). ");
        sb.append("Key features: micro-steps (break tasks into tiny actions), focus timer, focus companion ");
        sb.append("(virtual companion during work), rescue mode (for overwhelm — breathing, grounding, ");
        sb.append("5-min micro-session), context breadcrumbs (remember where you left off), ");
        sb.append("energy tracking, goals, and gentle nudges.\n\n");

        sb.append("## How to help\n");
        sb.append("- Answer questions about how the app works\n");
        sb.append("- Show task information when asked\n");
        sb.append("- Give practical productivity advice\n");
        sb.append("- Be encouraging, never judgmental\n");
        sb.append("- Keep responses concise — users lose focus on walls of text\n");
        sb.append("- When the user asks to see tasks, format them as a clean list\n");
        sb.append("- If a question is ambiguous, ask a brief clarifying question\n\n");

        // User's data context
        sb.append("## User's current data\n");
        sb.append("Name: ").append(user.getDisplayName()).append("\n");
        sb.append("Profile: ").append(user.getProfile()).append("\n\n");

        // Tasks summary
        List<Task> pending = taskRepository.findAllPendingForUser(user.getId());
        sb.append("### Pending tasks (").append(pending.size()).append(")\n");
        for (Task t : pending) {
            sb.append("- \"").append(t.getTitle()).append("\"");
            if (t.getDomain() != null) sb.append(" [").append(t.getDomain().getName()).append("]");
            sb.append(" | ").append(t.getUrgency()).append("/").append(t.getImportance());
            if (t.getDueDate() != null) {
                long hoursUntil = Duration.between(Instant.now(), t.getDueDate()).toHours();
                if (hoursUntil < 0) sb.append(" | OVERDUE");
                else if (hoursUntil < 24) sb.append(" | due in ").append(hoursUntil).append("h");
                else sb.append(" | due in ").append(hoursUntil / 24).append(" days");
            }
            if (t.getLastContext() != null) sb.append(" | left off: \"").append(t.getLastContext()).append("\"");
            sb.append("\n");
        }

        // Recently completed
        Instant oneWeekAgo = Instant.now().minus(Duration.ofDays(7));
        long completedThisWeek = taskRepository.countCompletedSince(user.getId(), oneWeekAgo);
        sb.append("\n### This week: ").append(completedThisWeek).append(" tasks completed\n");

        // Snoozed tasks
        List<Task> snoozed = taskRepository.findByUserIdAndStatusAndParentTaskIsNull(
                user.getId(), Task.Status.SNOOZED,
                org.springframework.data.domain.PageRequest.of(0, 20)).getContent();
        if (!snoozed.isEmpty()) {
            sb.append("\n### Snoozed tasks (").append(snoozed.size()).append(")\n");
            for (Task t : snoozed) {
                sb.append("- \"").append(t.getTitle()).append("\"");
                if (t.getSnoozedUntil() != null) {
                    long minsUntil = Duration.between(Instant.now(), t.getSnoozedUntil()).toMinutes();
                    if (minsUntil > 0) sb.append(" | snoozed for ").append(minsUntil).append("m more");
                    else sb.append(" | snooze expired");
                }
                sb.append("\n");
            }
        }

        // Goals
        List<Goal> goals = goalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                user.getId(), Goal.Status.ACTIVE);
        if (!goals.isEmpty()) {
            sb.append("\n### Active goals\n");
            for (Goal g : goals) {
                sb.append("- ").append(g.getTitle()).append("\n");
            }
        }

        // Domains
        List<LifeDomain> domains = domainRepository.findByUserIdOrderByPositionAsc(user.getId());
        if (!domains.isEmpty()) {
            sb.append("\n### Life domains: ");
            sb.append(domains.stream().map(LifeDomain::getName).collect(Collectors.joining(", ")));
            sb.append("\n");
        }

        return sb.toString();
    }

    private String toneDescription(User.Tone tone) {
        return switch (tone) {
            case SUPPORTIVE -> "warm, encouraging, and supportive";
            case PEER -> "direct, matter-of-fact, and peer-like";
            case PLAYFUL -> "light, fun, and gently humorous";
        };
    }

    private String callAi(User user, String apiKey, String systemPrompt, List<ChatMessage> history, String userMessage) {
        if (user.getAiProvider() == null || apiKey == null) return null;

        return switch (user.getAiProvider()) {
            case CLAUDE -> callClaude(apiKey, systemPrompt, history, userMessage);
            case OPENAI -> callOpenAi(apiKey, systemPrompt, history, userMessage);
            case GEMINI -> callGemini(apiKey, systemPrompt, history, userMessage);
        };
    }

    private String callClaude(String apiKey, String systemPrompt, List<ChatMessage> history, String userMessage) {
        StringBuilder messages = new StringBuilder("[");
        for (ChatMessage msg : history) {
            messages.append(String.format("{\"role\":\"%s\",\"content\":\"%s\"},",
                    msg.role(), escapeJson(msg.content())));
        }
        messages.append(String.format("{\"role\":\"user\",\"content\":\"%s\"}", escapeJson(userMessage)));
        messages.append("]");

        String body = String.format(
                "{\"model\":\"claude-haiku-4-5-20251001\",\"max_tokens\":1000,\"system\":\"%s\",\"messages\":%s}",
                escapeJson(systemPrompt), messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return sendAndParse(request, root -> root.path("content").get(0).path("text").asText());
    }

    private String callOpenAi(String apiKey, String systemPrompt, List<ChatMessage> history, String userMessage) {
        StringBuilder messages = new StringBuilder("[");
        messages.append(String.format("{\"role\":\"system\",\"content\":\"%s\"},", escapeJson(systemPrompt)));
        for (ChatMessage msg : history) {
            messages.append(String.format("{\"role\":\"%s\",\"content\":\"%s\"},",
                    msg.role(), escapeJson(msg.content())));
        }
        messages.append(String.format("{\"role\":\"user\",\"content\":\"%s\"}", escapeJson(userMessage)));
        messages.append("]");

        String body = String.format(
                "{\"model\":\"gpt-4o-mini\",\"max_tokens\":1000,\"messages\":%s}", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return sendAndParse(request, root -> root.path("choices").get(0).path("message").path("content").asText());
    }

    private String callGemini(String apiKey, String systemPrompt, List<ChatMessage> history, String userMessage) {
        StringBuilder parts = new StringBuilder("[");
        parts.append(String.format("{\"text\":\"%s\\n\\n%s\"}", escapeJson(systemPrompt), escapeJson(userMessage)));
        parts.append("]");

        String body = String.format("{\"contents\":[{\"parts\":%s}]}", parts);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return sendAndParse(request, root ->
                root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText());
    }

    private String sendAndParse(HttpRequest request, java.util.function.Function<JsonNode, String> extractor) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("AI chat error {}: {}", response.statusCode(), response.body());
                return null;
            }
            JsonNode root = mapper.readTree(response.body());
            return extractor.apply(root);
        } catch (Exception e) {
            log.error("AI chat call failed", e);
            return null;
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    public record ChatMessage(String role, String content) {}
}
