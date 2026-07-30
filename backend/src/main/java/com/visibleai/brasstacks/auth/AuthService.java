package com.visibleai.brasstacks.auth;

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

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final LifeDomainRepository domainRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       LifeDomainRepository domainRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.domainRepository = domainRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
        user = userRepository.save(user);

        seedDefaultDomains(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        if (!jwtUtil.isValid(token) || !jwtUtil.isRefreshToken(token)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String email = jwtUtil.extractEmail(token);
        Long userId = jwtUtil.extractUserId(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId());
        return new AuthResponse(user.getId(), newAccessToken, null, 3600);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId());
        return new AuthResponse(user.getId(), accessToken, refreshToken, 3600);
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
