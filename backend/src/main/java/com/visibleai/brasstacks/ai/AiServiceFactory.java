package com.visibleai.brasstacks.ai;

import com.visibleai.brasstacks.model.User;
import org.springframework.stereotype.Component;

@Component
public class AiServiceFactory {

    public AiService forUser(User user) {
        if (!user.isAiEnabled() || user.getAiProvider() == null || user.getAiApiKey() == null) {
            return null;
        }
        return switch (user.getAiProvider()) {
            case CLAUDE -> new ClaudeAiService(user.getAiApiKey());
            case OPENAI -> new OpenAiService(user.getAiApiKey());
            case GEMINI -> new GeminiAiService(user.getAiApiKey());
        };
    }
}
