package com.visibleai.brasstacks.ai;

import com.visibleai.brasstacks.config.JwtAuthenticationFilter;
import com.visibleai.brasstacks.model.User;
import com.visibleai.brasstacks.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final AiUsageTracker aiUsageTracker;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, AiUsageTracker aiUsageTracker,
                          UserRepository userRepository) {
        this.chatService = chatService;
        this.aiUsageTracker = aiUsageTracker;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(Authentication auth, @RequestBody ChatRequest request) {
        Long userId = (Long) auth.getCredentials();
        User user = userRepository.findById(userId).orElseThrow();

        if (!user.isAiEnabled() || user.getAiProvider() == null || user.getAiApiKey() == null) {
            return ResponseEntity.ok(Map.of(
                    "reply", "To use the assistant, enable AI in Settings and add your API key (Claude, OpenAI, or Gemini). " +
                             "Go to Settings > AI Features to get started."
            ));
        }

        if (!aiUsageTracker.isGlobalEnabled()) {
            return ResponseEntity.ok(Map.of("reply", "AI features are temporarily unavailable. Please try again later."));
        }

        if (!aiUsageTracker.canUse(userId)) {
            return ResponseEntity.ok(Map.of("reply", "You've reached your AI usage limit for now. Try again in a bit."));
        }

        // Decrypt the API key using the derived key from the JWT
        JwtAuthenticationFilter.AuthDetails details =
                (JwtAuthenticationFilter.AuthDetails) auth.getDetails();
        String derivedKey = details.derivedKey();

        String apiKey;
        try {
            apiKey = ApiKeyEncryptor.isEncrypted(user.getAiApiKey())
                    ? ApiKeyEncryptor.decrypt(user.getAiApiKey(), derivedKey)
                    : user.getAiApiKey();
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "reply", "Unable to decrypt your API key. Please re-enter it in Settings > AI Features."));
        }

        List<ChatService.ChatMessage> history = request.history() != null
                ? request.history().stream()
                    .map(m -> new ChatService.ChatMessage(m.role(), m.content()))
                    .toList()
                : List.of();

        String reply = chatService.chat(user, apiKey, history, request.message());
        aiUsageTracker.recordUsage(userId, user.getAiProvider().name());

        return ResponseEntity.ok(Map.of("reply", reply));
    }

    public record ChatRequest(String message, List<MessageItem> history) {}
    public record MessageItem(String role, String content) {}
}
