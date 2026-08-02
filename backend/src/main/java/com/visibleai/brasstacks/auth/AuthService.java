package com.visibleai.brasstacks.auth;

import com.visibleai.brasstacks.ai.ApiKeyEncryptor;
import com.visibleai.brasstacks.auth.dto.AuthResponse;
import com.visibleai.brasstacks.auth.dto.LoginRequest;
import com.visibleai.brasstacks.auth.dto.RefreshRequest;
import com.visibleai.brasstacks.auth.dto.RegisterRequest;
import com.visibleai.brasstacks.model.LifeDomain;
import com.visibleai.brasstacks.model.User;
import com.visibleai.brasstacks.repository.LifeDomainRepository;
import com.visibleai.brasstacks.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class AuthService {

    private static final Duration VERIFICATION_EXPIRY = Duration.ofHours(24);

    private final UserRepository userRepository;
    private final LifeDomainRepository domainRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       LifeDomainRepository domainRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.domainRepository = domainRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.displayName(),
                request.profile()
        );

        // Generate verification token
        String token = generateToken();
        user.setVerificationToken(token);
        user.setVerificationExpiry(Instant.now().plus(VERIFICATION_EXPIRY));

        user = userRepository.save(user);
        seedDefaultDomains(user);

        // Send verification email (async-safe: doesn't affect the transaction)
        emailService.sendVerificationEmail(user.getEmail(), user.getDisplayName(), token);

        // Return tokens so the app can proceed — user has a grace period
        // until the access token expires, then login/refresh will block
        String derivedKey = ApiKeyEncryptor.deriveKey(request.password(), user.getEmail());
        return buildAuthResponse(user, derivedKey);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new BadCredentialsException(
                    "Please verify your email before signing in. Check your inbox for a verification link.");
        }

        String derivedKey = ApiKeyEncryptor.deriveKey(request.password(), user.getEmail());

        // Migrate any plaintext API key to encrypted format
        if (user.getAiApiKey() != null && !ApiKeyEncryptor.isEncrypted(user.getAiApiKey())) {
            user.setAiApiKey(ApiKeyEncryptor.encrypt(user.getAiApiKey(), derivedKey));
        }

        // Track last login
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return buildAuthResponse(user, derivedKey);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        if (!jwtUtil.isValid(token) || !jwtUtil.isRefreshToken(token)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String email = jwtUtil.extractEmail(token);
        String derivedKey = jwtUtil.extractDerivedKey(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isEmailVerified()) {
            throw new BadCredentialsException(
                    "Please verify your email before continuing. Check your inbox for a verification link.");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId(), derivedKey);
        return new AuthResponse(user.getId(), newAccessToken, null, 3600);
    }

    @Transactional
    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token).orElse(null);
        if (user == null) return false;

        if (user.isEmailVerified()) return true; // already verified

        if (user.getVerificationExpiry() != null && Instant.now().isAfter(user.getVerificationExpiry())) {
            return false; // expired
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpiry(null);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.isEmailVerified()) return;

        String token = generateToken();
        user.setVerificationToken(token);
        user.setVerificationExpiry(Instant.now().plus(VERIFICATION_EXPIRY));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getDisplayName(), token);
    }

    private AuthResponse buildAuthResponse(User user, String derivedKey) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId(), derivedKey);
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId(), derivedKey);
        return new AuthResponse(user.getId(), accessToken, refreshToken, 3600);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void seedDefaultDomains(User user) {
        List<LifeDomain> domains = switch (user.getProfile()) {
            case EXECUTIVE -> List.of(
                    new LifeDomain(user, "Work", "#6366F1", 70, 0),
                    new LifeDomain(user, "Family", "#EC4899", 50, 1),
                    new LifeDomain(user, "Personal", "#14B8A6", 40, 2),
                    new LifeDomain(user, "Financial", "#F59E0B", 50, 3),
                    new LifeDomain(user, "Health", "#22C55E", 40, 4)
            );
            case PROFESSIONAL -> List.of(
                    new LifeDomain(user, "Work", "#6366F1", 60, 0),
                    new LifeDomain(user, "Personal", "#14B8A6", 50, 1),
                    new LifeDomain(user, "Health", "#22C55E", 40, 2)
            );
            case STUDENT -> List.of(
                    new LifeDomain(user, "Study", "#6366F1", 70, 0),
                    new LifeDomain(user, "Personal", "#14B8A6", 50, 1),
                    new LifeDomain(user, "Health", "#22C55E", 40, 2)
            );
        };
        domainRepository.saveAll(domains);
    }
}
