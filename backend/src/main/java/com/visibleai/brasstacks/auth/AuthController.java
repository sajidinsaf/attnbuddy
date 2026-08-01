package com.visibleai.brasstacks.auth;

import com.visibleai.brasstacks.auth.dto.AuthResponse;
import com.visibleai.brasstacks.auth.dto.LoginRequest;
import com.visibleai.brasstacks.auth.dto.RefreshRequest;
import com.visibleai.brasstacks.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/verify", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean success = authService.verifyEmail(token);
        String html = success ? VERIFY_SUCCESS_HTML : VERIFY_FAILURE_HTML;
        return ResponseEntity.ok(html);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@RequestBody ResendRequest request) {
        authService.resendVerification(request.email());
        // Always return 200 to avoid leaking whether an email is registered
        return ResponseEntity.ok().build();
    }

    public record ResendRequest(String email) {}

    private static final String VERIFY_SUCCESS_HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Email Verified</title>
            <style>body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#0F172A;color:#E2E8F0;display:flex;justify-content:center;align-items:center;min-height:100vh;margin:0;}
            .card{background:#1E293B;border:1px solid #334155;border-radius:12px;padding:48px;text-align:center;max-width:400px;}
            h1{color:#22C55E;font-size:28px;margin-bottom:12px;}p{color:#94A3B8;font-size:15px;line-height:1.6;}
            .btn{display:inline-block;background:#6366F1;color:#F8FAFC;font-size:16px;font-weight:600;padding:14px 32px;border-radius:8px;text-decoration:none;margin-top:24px;}</style>
            </head><body><div class="card">
            <h1>Email Verified</h1>
            <p>Your email has been verified. You can now sign in to Brasstacks.</p>
            <a href="brasstacks://login" class="btn">Open Brasstacks</a>
            </div></body></html>
            """;

    private static final String VERIFY_FAILURE_HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Verification Failed</title>
            <style>body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#0F172A;color:#E2E8F0;display:flex;justify-content:center;align-items:center;min-height:100vh;margin:0;}
            .card{background:#1E293B;border:1px solid #334155;border-radius:12px;padding:48px;text-align:center;max-width:400px;}
            h1{color:#F59E0B;font-size:28px;margin-bottom:12px;}p{color:#94A3B8;font-size:15px;line-height:1.6;}</style>
            </head><body><div class="card">
            <h1>Link Expired or Invalid</h1>
            <p>This verification link has expired or is no longer valid. Please open Brasstacks and request a new verification email.</p>
            </div></body></html>
            """;
}
