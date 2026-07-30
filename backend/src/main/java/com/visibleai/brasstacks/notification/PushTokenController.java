package com.visibleai.brasstacks.notification;

import com.visibleai.brasstacks.model.PushToken;
import com.visibleai.brasstacks.repository.PushTokenRepository;
import com.visibleai.brasstacks.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push-token")
public class PushTokenController {

    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;

    public PushTokenController(PushTokenRepository pushTokenRepository,
                                UserRepository userRepository) {
        this.pushTokenRepository = pushTokenRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Void> register(Authentication auth, @RequestBody PushTokenRequest request) {
        Long userId = (Long) auth.getCredentials();

        // Upsert: if token already exists, update its user assignment
        var existing = pushTokenRepository.findByToken(request.token());
        if (existing.isPresent()) {
            PushToken pt = existing.get();
            if (!pt.getUser().getId().equals(userId)) {
                pt.setUser(userRepository.getReferenceById(userId));
            }
            pt.setDeviceName(request.deviceName());
            pushTokenRepository.save(pt);
        } else {
            pushTokenRepository.save(new PushToken(
                    userRepository.getReferenceById(userId),
                    request.token(),
                    request.deviceName()
            ));
        }

        return ResponseEntity.ok().build();
    }

    public record PushTokenRequest(String token, String deviceName) {}
}
