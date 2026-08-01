package com.visibleai.brasstacks.ai;

import com.visibleai.brasstacks.model.User;
import org.springframework.stereotype.Component;

@Component
public class AiServiceFactory {

    public AiService forUser(User user, String decryptedApiKey) {
        if (!user.isAiEnabled() || user.getAiProvider() == null || decryptedApiKey == null) {
            return null;
        }
        return switch (user.getAiProvider()) {
            case CLAUDE -> new ClaudeAiService(decryptedApiKey);
            case OPENAI -> new OpenAiService(decryptedApiKey);
            case GEMINI -> new GeminiAiService(decryptedApiKey);
        };
    }
}
