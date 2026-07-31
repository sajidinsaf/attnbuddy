package com.visibleai.brasstacks.auth;

import com.visibleai.brasstacks.model.User;
import com.visibleai.brasstacks.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final UserRepository userRepository;

    public SettingsController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/nudges")
    public NudgeSettings getNudgeSettings(Authentication auth) {
        User user = userRepository.findById(getUserId(auth)).orElseThrow();
        return new NudgeSettings(
                user.getNudgeFrequency().name(),
                user.getQuietStart(),
                user.getQuietEnd()
        );
    }

    @PutMapping("/nudges")
    @Transactional
    public NudgeSettings updateNudgeSettings(Authentication auth, @RequestBody NudgeSettings request) {
        User user = userRepository.findById(getUserId(auth)).orElseThrow();
        user.setNudgeFrequency(User.NudgeFrequency.valueOf(request.nudgeFrequency()));
        if (request.quietStart() != null && request.quietEnd() != null) {
            user.setQuietStart(request.quietStart());
            user.setQuietEnd(request.quietEnd());
        } else {
            user.setQuietStart(null);
            user.setQuietEnd(null);
        }
        userRepository.save(user);
        return request;
    }

    @GetMapping("/tone")
    public ToneSettings getTone(Authentication auth) {
        User user = userRepository.findById(getUserId(auth)).orElseThrow();
        return new ToneSettings(user.getTone().name());
    }

    @PutMapping("/tone")
    @Transactional
    public ToneSettings updateTone(Authentication auth, @RequestBody ToneSettings request) {
        User user = userRepository.findById(getUserId(auth)).orElseThrow();
        user.setTone(User.Tone.valueOf(request.tone()));
        userRepository.save(user);
        return request;
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getCredentials();
    }

    public record NudgeSettings(String nudgeFrequency, String quietStart, String quietEnd) {}
    public record ToneSettings(String tone) {}
}
