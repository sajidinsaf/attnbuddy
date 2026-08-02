package com.visibleai.brasstacks.auth;

import com.visibleai.brasstacks.model.User;
import com.visibleai.brasstacks.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class UnverifiedAccountCleanup {

    private static final Logger log = LoggerFactory.getLogger(UnverifiedAccountCleanup.class);
    private static final Duration MAX_AGE = Duration.ofDays(7);

    private final UserRepository userRepository;

    public UnverifiedAccountCleanup(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 3 * * *") // 3 AM daily
    @Transactional
    public void cleanupUnverifiedAccounts() {
        Instant cutoff = Instant.now().minus(MAX_AGE);
        List<User> stale = userRepository.findByEmailVerifiedFalseAndCreatedAtBefore(cutoff);

        if (!stale.isEmpty()) {
            log.info("Deleting {} unverified accounts older than 7 days", stale.size());
            userRepository.deleteAll(stale);
        }
    }
}
